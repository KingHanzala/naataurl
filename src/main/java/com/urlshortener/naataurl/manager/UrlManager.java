package com.urlshortener.naataurl.manager;

import com.urlshortener.naataurl.persistence.model.UrlMapper;
import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.response.GetUrlInfoResponse;
import com.urlshortener.naataurl.response.GetUserDashboardResponse;
import com.urlshortener.naataurl.response.UrlResponse;
import com.urlshortener.naataurl.response.UserResponse;
import com.urlshortener.naataurl.service.UrlService;
import com.urlshortener.naataurl.service.UserService;
import com.urlshortener.naataurl.utils.UrlMapperHelper;
import com.urlshortener.naataurl.service.GoogleSafeBrowsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UrlManager {

    private @Autowired RedisManager redisManager;

    private @Autowired UrlService urlService;

    private @Autowired UserService userService;

    private @Autowired UrlMapperHelper urlMapperHelper;

    private @Autowired GoogleSafeBrowsingService safeBrowsingService;

    private static final Logger logger = LoggerFactory.getLogger(UrlManager.class);

    public String getOriginalUrl(String shortUrl){
        /*
         * Get Original URL
         */
        //Check if URL exists in Redis
        if(shortUrl == null){
            return null;
        }
        String originalUrl = redisManager.getOriginalUrl(shortUrl);
        UrlMapper urlMapper = null;
        //If not, check in db
        if(originalUrl == null) {
            urlMapper = urlService.findByShortUrl(shortUrl);
            if (urlMapper == null) {
                return null;
            }
            originalUrl = urlMapper.getOriginalUrl();

            //Add URL mapping in Redis
            redisManager.addShortUrlToCache(shortUrl, originalUrl);
        }

        /*
         * Increment click
         */

        //Check if clicks exist in Redis
        Long clicks = redisManager.getUrlClicks(shortUrl);
        //Fetch and increment in db if Key does not exist
        if(clicks == null){
            if(urlMapper == null) {
                urlMapper = urlService.findByShortUrl(shortUrl);
            }
            clicks = urlMapper.getUrlClicks();
            urlMapper.setUrlClicks(clicks+1);
            urlService.saveUrlMapper(urlMapper);
        }
        //Add or Update cache
        redisManager.addClickToCache(shortUrl, clicks + 1);
        return originalUrl;
    }

    public Long getUrlClicks(String shortUrl){
        Long clicks = redisManager.getUrlClicks(shortUrl);
        if(clicks == null){
            UrlMapper urlMapper = urlService.findByShortUrl(shortUrl);
            clicks = urlMapper.getUrlClicks();
            redisManager.addClickToCache(shortUrl, clicks);
        }
        return clicks;
    }

    public UrlResponse createUrlMapper(String originalUrl, Long userId) throws Exception {
        // Check if URL is safe using Google Safe Browsing
        if (!safeBrowsingService.isUrlSafe(originalUrl)) {
            throw new IllegalArgumentException("URL is not safe according to Google Safe Browsing");
        }

        UrlMapper urlMapper = urlService.findByOriginalUrl(originalUrl, userId);
        UrlResponse urlResponse = null;
        User user = userService.findByUserId(userId);
        if (urlMapper != null) {
            urlResponse = new UrlResponse(urlMapper.getShortUrl(), user.getUsageCredits());
            return urlResponse;
        }
        if (user == null) {
            logger.info("User not found");
            return null;
        } else if (user.getUsageCredits() == 0) {
            throw new Exception();
        } else {
            user.decrementCredits();
        }

        Long urlId = urlService.getNextUrlId();
        String shortUrl = null;
        try {
            shortUrl = urlMapperHelper.hashUrl(urlId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
        urlMapper = new UrlMapper();
        urlMapper.setUrlId(urlId);
        urlMapper.setUserId(userId);
        urlMapper.setOriginalUrl(originalUrl);
        urlMapper.setShortUrl(shortUrl);
        urlMapper.setCreatedAt(new Date());
        handleCreateUrlMapper(userId, new GetUrlInfoResponse(originalUrl,shortUrl,0L,urlMapper.getCreatedAt(),userId));
        redisManager.addShortUrlToCache(shortUrl,originalUrl);
        try {
            urlService.saveUrlMapper(urlMapper);
        } catch (Exception e) {
            user.incrementCredits();
            throw new RuntimeException(e);
        }
        userService.saveUser(user);
        return new UrlResponse(shortUrl,user.getUsageCredits());
    }

    public GetUserDashboardResponse getUserDashboardResponseFromDb(Long userId){
        User user = userService.findByUserId(userId);
        if(user == null){
            return null;
        }
        List<GetUrlInfoResponse> urls = urlService.findByUserId(userId).stream()
                .map(urlMapper -> {
                    GetUrlInfoResponse urlInfo = new GetUrlInfoResponse();
                    urlInfo.setOriginalUrl(urlMapper.getOriginalUrl());
                    urlInfo.setShortUrl(urlMapper.getShortUrl());
                    urlInfo.setUrlClicks(urlMapper.getUrlClicks());
                    urlInfo.setCreatedDtm(urlMapper.getCreatedAt());
                    urlInfo.setUserId(urlMapper.getUserId());
                    return urlInfo;
                })
                .collect(Collectors.toList());

        // Create response
        GetUserDashboardResponse getUserDashboardResponse = new GetUserDashboardResponse();
        getUserDashboardResponse.setUserResponse(new UserResponse(user.getUserId(), user.getUserName(), user.getUserEmail()));
        getUserDashboardResponse.setUrlsMappedList(urls);
        getUserDashboardResponse.setAvailableCredits(user.getUsageCredits());
        return getUserDashboardResponse;
    }

    public GetUserDashboardResponse getUserDashboardResponse(Long userId){
        GetUserDashboardResponse getUserDashboardResponse = redisManager.getUserDashboardResponse(String.valueOf(userId));

        if(getUserDashboardResponse == null){
            getUserDashboardResponse = getUserDashboardResponseFromDb(userId);
            redisManager.saveUserDashboard(String.valueOf(userId), getUserDashboardResponse);
        } else {
            List<GetUrlInfoResponse> getUrlInfoResponses = getUserDashboardResponse.getUrlsMappedList();
            for(GetUrlInfoResponse getUrlInfoResponse: getUrlInfoResponses){
                Long clicks = getUrlClicks(getUrlInfoResponse.getShortUrl());
                getUrlInfoResponse.setUrlClicks(clicks);
            }
        }
        return getUserDashboardResponse;
    }

    public void handleCreateUrlMapper(Long userId, GetUrlInfoResponse getUrlInfoResponse){
        if(userId == null || getUrlInfoResponse == null) {
            return;
        }
        try {
            saveUrlInfoResponseToRedis(userId.toString(), getUrlInfoResponse);
        } catch(Exception e){
            logger.error("Unable to save url info for user id {} in redis, Exception {}",userId, e.getMessage());
        }
    }

    public void saveUrlInfoResponseToRedis(String userId, GetUrlInfoResponse response) {
        GetUserDashboardResponse getUserDashboardResponse = redisManager.getUserDashboardResponse(userId);
        if(getUserDashboardResponse == null){
            getUserDashboardResponse = getUserDashboardResponseFromDb(Long.parseLong(userId));
        }
        if(getUserDashboardResponse == null) return;

        List<GetUrlInfoResponse> getUrlInfoResponseList = getUserDashboardResponse.getUrlsMappedList();
        getUrlInfoResponseList.add(response);
        getUserDashboardResponse.setAvailableCredits(getUserDashboardResponse.getAvailableCredits() - 1);
        redisManager.saveUserDashboard(userId, getUserDashboardResponse);
    }
}
