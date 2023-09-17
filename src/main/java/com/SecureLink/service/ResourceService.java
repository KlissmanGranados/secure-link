package com.SecureLink.service;

import com.SecureLink.dto.ResourceDto;
import com.SecureLink.dto.ResourceEncodeDto;
import com.SecureLink.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    private static final Map<String, MediaType> extensionToMediaTypeMap = new HashMap<>() {{
        put("jpeg", MediaType.IMAGE_JPEG);
        put("jpg", MediaType.IMAGE_JPEG);
        put("png", MediaType.IMAGE_PNG);
        put("gif", MediaType.IMAGE_GIF);
        put("bmp", MediaType.valueOf("image/bmp"));
        put("mp3", MediaType.valueOf("audio/mpeg"));
    }};
    private final AtomicInteger counter = new AtomicInteger();
    private final Map<String, ResourceDto> storage = new HashMap<>();

    public Map<String, ResourceDto> getStorage(){
        return storage;
    }

    public ResourceEncodeDto uriEncoder(ResourceDto resourceDto) {
        String key = generateKey();
        storage.put(key, resourceDto);
        return new ResourceEncodeDto(key, resourceDto.getExpired());
    }

    public ResponseEntity<byte[]> getResource(String key) {

        ResourceDto resourceDto = Optional.ofNullable( storage.get(key) )
                .orElseThrow( () -> new BadRequestException("resource not found!") );

        if(isExpired(resourceDto)) {
            throw new BadRequestException("Resource is expired!");
        }

        String url = resourceDto.getUri();
        byte[] data = getBytesFromUrl(url);
        HttpHeaders headers = new HttpHeaders();
        headers.setExpires(resourceDto.getExpirationDate().getTime());
        headers.setContentType(getMediaType(url));
        return new ResponseEntity<>(data, headers, HttpStatus.OK);

    }

    private MediaType getMediaType(String url) {
        String fileExtension = url.substring(url.lastIndexOf(".") + 1);
        return extensionToMediaTypeMap.getOrDefault(fileExtension, MediaType.APPLICATION_OCTET_STREAM);
    }

    private byte[] getBytesFromUrl(String url) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        return response.getBody();
    }

    private String generateKey() {

        try {

            int id = counter.incrementAndGet();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(Integer.toString(id)
                    .concat(String.valueOf(LocalDateTime.now().getNano()))
                    .getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, encodedhash));

        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            logger.error(noSuchAlgorithmException.toString());
            throw new BadRequestException("An error occurred while trying to process the request, please try again");
        }

    }

    private boolean isExpired(ResourceDto resourceDto) {
        Date expiredAt = resourceDto.getExpirationDate();
        Date now = new Date();
        return expiredAt.before(now);
    }
}
