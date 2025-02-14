package com.lookingdev.stackoverflow.Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lookingdev.stackoverflow.Domain.Models.DeveloperDTOModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StackOverflowService {

    @Value("${stackoverflow.apikey}")
    private String API_KEY;

    private static final String BASE_URL = "https://api.stackexchange.com/2.3/users";
    private static final String SITE = "stackoverflow";
    private static final Logger LOGGER = LoggerFactory.getLogger(StackOverflowService.class);
    private static final int USER_COUNT_IN_DB = 6;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    private final List<DeveloperDTOModel> cachedDevelopers = new ArrayList<>();

    public List<DeveloperDTOModel> fetchUsers() {
        if (!cachedDevelopers.isEmpty()) {
            LOGGER.info("Returning cached developer data.");
            return cachedDevelopers;
        }

        String url = BASE_URL + "?site=" + SITE + "&pagesize=" + USER_COUNT_IN_DB + "&key=" + API_KEY;
        List<DeveloperDTOModel> developers = new ArrayList<>();

        try (Response response = executeRequest(url)) {
            if (response.isSuccessful() && response.body() != null) {
                developers = parseUsers(response.body().string());
                LOGGER.info("Successfully fetched and parsed {} users.", developers.size());
                cachedDevelopers.addAll(developers);
            } else {
                LOGGER.error("Error fetching users: {}", response.code());
            }
        } catch (IOException e) {
            LOGGER.error("Exception during fetching users: {}", e.getMessage());
        }

        return developers;
    }

    private List<DeveloperDTOModel> parseUsers(String jsonResponse) {
        List<DeveloperDTOModel> developerList = new ArrayList<>();

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray usersArray = jsonObject.getAsJsonArray("items");

            for (int i = 0; i < usersArray.size(); i++) {
                JsonObject userObject = usersArray.get(i).getAsJsonObject();

                String username = getStringField(userObject, "display_name");
                String profileUrl = getStringField(userObject, "link");
                int reputation = getIntField(userObject, "reputation");
                String location = getStringField(userObject, "location");
                LocalDate lastActivityDate = getLastActivityDate(userObject);

                int userId = userObject.get("user_id").getAsInt();

                List<String> skills = fetchUserTags(userId);

                DeveloperDTOModel developer = new DeveloperDTOModel(
                        "StackOverflow",
                        username,
                        profileUrl,
                        reputation,
                        skills,
                        location,
                        lastActivityDate
                );

                developerList.add(developer);
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing user data: {}", e.getMessage());
        }

        return developerList;
    }

    private List<String> fetchUserTags(int userId) {
        LOGGER.info("Fetching tags for user {}", userId);

        String url = "https://api.stackexchange.com/2.3/users/" + userId + "/tags?site=" + SITE + "&key=" + API_KEY;
        List<String> tags = new ArrayList<>();

        for (int attempt = 1; attempt <= 3; attempt++) {
            try (Response response = executeRequest(url)) {
                if (response.body() == null) {
                    LOGGER.warn("Empty response for tags request. Attempt {}/3", attempt);
                    continue;
                }

                String responseBody = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                if (jsonObject.has("backoff")) {
                    int backoffSeconds = jsonObject.get("backoff").getAsInt();
                    LOGGER.warn("Received backoff {}s for user {}. Retrying after delay...", backoffSeconds, userId);
                    Thread.sleep(backoffSeconds * 1000L);
                    continue;
                }

                return extractTags(jsonObject, userId);

            } catch (IOException | InterruptedException e) {
                LOGGER.error("Error fetching tags for user {}: {}. Attempt {}/3", userId, e.getMessage(), attempt);
                waitBeforeRetry();
            }
        }

        return tags;
    }

    private Response executeRequest(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        return client.newCall(request).execute();
    }

    private List<String> extractTags(JsonObject jsonObject, int userId) {
        List<String> tags = new ArrayList<>();

        if (!jsonObject.has("items")) {
            LOGGER.warn("No tags returned for user {}", userId);
            return tags;
        }

        JsonArray tagsArray = jsonObject.getAsJsonArray("items");
        for (int i = 0; i < tagsArray.size(); i++) {
            JsonObject tagObject = tagsArray.get(i).getAsJsonObject();
            tags.add(tagObject.get("name").getAsString());
        }

        return tags;
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    private String getStringField(JsonObject jsonObject, String field) {
        return jsonObject.has(field) ? jsonObject.get(field).getAsString() : null;
    }

    private int getIntField(JsonObject jsonObject, String field) {
        return jsonObject.has(field) ? jsonObject.get(field).getAsInt() : 0;
    }

    private LocalDate getLastActivityDate(JsonObject userObject) {
        if (userObject.has("last_access_date")) {
            long lastAccessDate = userObject.get("last_access_date").getAsLong();
            return Instant.ofEpochSecond(lastAccessDate).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }
}
