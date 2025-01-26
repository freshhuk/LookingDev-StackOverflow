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

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    // Кэш для сохранения тегов пользователей
    private final List<DeveloperDTOModel> cachedDevelopers = new ArrayList<>();

    // Метод для ограничения скорости запросов
    private void throttleRequests() {
        try {
            // Ограничиваем частоту запросов: 1 запрос каждые 34 мс (примерно 30 запросов/сек)
            Thread.sleep(34);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
        }
    }

    public List<DeveloperDTOModel> fetchUsers(int pageSize) {
        // Проверяем, если данные уже есть в кэше, возвращаем их
        if (!cachedDevelopers.isEmpty()) {
            LOGGER.info("Returning cached developer data.");
            return cachedDevelopers;
        }

        String url = BASE_URL + "?site=" + SITE + "&pagesize=" + 1 + "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response;
        List<DeveloperDTOModel> developers = new ArrayList<>();

        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                developers = parseUsers(responseBody);
                LOGGER.info("Successfully fetched and parsed {} users.", developers.size());

                // Сохраняем данные в кэш
                cachedDevelopers.addAll(developers);
            } else {
                LOGGER.error("Error fetching users: {}", response.code());
            }
        } catch (IOException e) {
            LOGGER.error("Exception during fetching users: {}", e.toString());
        }
        return developers;
    }

    private List<DeveloperDTOModel> parseUsers(String jsonResponse) {
        List<DeveloperDTOModel> developerList = new ArrayList<>();

        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray usersArray = jsonObject.getAsJsonArray("items");

        for (int i = 0; i < usersArray.size(); i++) {
            JsonObject userObject = usersArray.get(i).getAsJsonObject();

            String username = userObject.has("display_name") ? userObject.get("display_name").getAsString() : null;
            String profileUrl = userObject.has("link") ? userObject.get("link").getAsString() : null;
            int reputation = userObject.has("reputation") ? userObject.get("reputation").getAsInt() : 0;
            String location = userObject.has("location") ? userObject.get("location").getAsString() : null;

            // Convert last_activity_date to LocalDate
            LocalDate lastActivityDate = null;
            if (userObject.has("last_access_date")) {
                long lastAccessDate = userObject.get("last_access_date").getAsLong();
                lastActivityDate = Instant.ofEpochSecond(lastAccessDate).atZone(ZoneId.systemDefault()).toLocalDate();
            }

            // Получаем теги пользователя (ограничиваем запросы к API)
            int userId = userObject.get("user_id").getAsInt();
            List<String> skills = fetchUserTags(userId);

            DeveloperDTOModel developer = new DeveloperDTOModel(
                    "StackOverflow",  // platform
                    username,
                    profileUrl,
                    reputation,
                    skills,
                    location,
                    lastActivityDate
            );

            developerList.add(developer);
        }

        return developerList;
    }

    private List<String> fetchUserTags(int userId) {
        throttleRequests(); // Ограничиваем частоту запросов

        String url = "https://api.stackexchange.com/2.3/users/" + userId + "/tags?site=" + SITE + "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        List<String> tags = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 429) { // Если API вернул Too Many Requests
                LOGGER.warn("Received 429 Too Many Requests for user {}. Retrying in 1 second...", userId);
                Thread.sleep(1000); // Ждём 1 секунду и повторяем запрос
                return fetchUserTags(userId);
            }

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray tagsArray = jsonObject.getAsJsonArray("items");

                for (int i = 0; i < tagsArray.size(); i++) {
                    JsonObject tagObject = tagsArray.get(i).getAsJsonObject();
                    String tagName = tagObject.get("name").getAsString();
                    tags.add(tagName);
                }
            } else {
                LOGGER.warn("Failed to fetch tags for user {}: {}", userId, response.code());
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Exception fetching tags for user {}: {}", userId, e.toString());
        }
        return tags;
    }
}
