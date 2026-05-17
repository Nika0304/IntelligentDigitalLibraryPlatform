package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExternalBooksService
{
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> searchBooks(String q)
    {
        List<Map<String, Object>> googleResults = searchGoogleBooks(q);

        if(!googleResults.isEmpty())
        {
            return googleResults;
        }

        return searchOpenLibrary(q);
    }

    private List<Map<String, Object>> searchGoogleBooks(String q)
    {
        try
        {
            String key = System.getenv("GOOGLE_BOOKS_API_KEY");

            String url = "https://www.googleapis.com/books/v1/volumes"
                    + "?q=" + q.trim().replace(" ", "+")
                    + "&maxResults=12&printType=books"
                    + (key != null && !key.isEmpty() ? "&key=" + key : "");

            String body = rest.getForObject(url, String.class);
            JsonNode root = mapper.readTree(body);
            JsonNode items = root.path("items");

            List<Map<String, Object>> result = new ArrayList<>();

            if(items.isArray() && items.size() > 0)
            {
                for(JsonNode it : items)
                {
                    JsonNode v = it.path("volumeInfo");

                    Map<String, Object> m = new HashMap<>();
                    m.put("externalId", it.path("id").asText(""));
                    m.put("source", "google");
                    m.put("title", v.path("title").asText("Fără titlu"));
                    m.put("authors", arrayToList(v.path("authors")));
                    m.put("description", v.path("description").asText(""));

                    String pub = v.path("publishedDate").asText("");
                    m.put("publishedDate", pub);
                    m.put("publicationYear", extractYear(pub));

                    m.put("categories", arrayToList(v.path("categories")));

                    String thumb = v.path("imageLinks").path("thumbnail").asText("");
                    m.put("coverImageURL", thumb.replace("http:", "https:"));

                    m.put("pageCount", v.path("pageCount").asInt(0));
                    m.put("language", v.path("language").asText(""));

                    result.add(m);
                }
            }

            return result;
        }
        catch(Exception e)
        {
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> searchOpenLibrary(String q)
    {
        try
        {
            String encodedQuery = URLEncoder.encode(q.trim(), StandardCharsets.UTF_8);

            String url = "https://openlibrary.org/search.json?q=" + encodedQuery
                    + "&limit=12&fields=key,title,author_name,first_publish_year,cover_i,subject,number_of_pages_median,language";

            String body = rest.getForObject(url, String.class);
            JsonNode root = mapper.readTree(body);
            JsonNode docs = root.path("docs");

            List<Map<String, Object>> result = new ArrayList<>();

            for(int i = 0; i < Math.min(12, docs.size()); i++)
            {
                JsonNode d = docs.get(i);

                String key = d.path("key").asText("");

                Map<String, Object> m = new HashMap<>();
                m.put("externalId", key);
                m.put("source", "openlibrary");
                m.put("title", d.path("title").asText("Fără titlu"));
                m.put("authors", arrayToList(d.path("author_name")));

                // NEW: încercăm să luăm descrierea din endpoint-ul detaliat OpenLibrary
                m.put("description", fetchOpenLibraryDescription(key));

                Integer year = d.has("first_publish_year")
                        ? d.get("first_publish_year").asInt()
                        : null;

                m.put("publishedDate", year != null ? String.valueOf(year) : "");
                m.put("publicationYear", year);

                m.put("categories", arrayToList(d.path("subject")));

                int coverId = d.path("cover_i").asInt(0);

                m.put("coverImageURL", coverId > 0
                        ? "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg"
                        : "");

                m.put("pageCount", d.path("number_of_pages_median").asInt(0));

                m.put("language", d.path("language").isArray() && d.path("language").size() > 0
                        ? d.path("language").get(0).asText("")
                        : "");

                result.add(m);
            }

            return result;
        }
        catch(Exception e)
        {
            throw new RuntimeException("Nu se poate accesa nicio sursă externă: " + e.getMessage());
        }
    }

    private String fetchOpenLibraryDescription(String key)
    {
        if(key == null || key.isBlank())
        {
            return "";
        }

        try
        {
            String detailsUrl = "https://openlibrary.org" + key + ".json";

            String body = rest.getForObject(detailsUrl, String.class);
            JsonNode root = mapper.readTree(body);

            JsonNode descriptionNode = root.path("description");

            if(descriptionNode.isTextual())
            {
                return descriptionNode.asText("");
            }

            if(descriptionNode.isObject())
            {
                return descriptionNode.path("value").asText("");
            }

            return "";
        }
        catch(Exception e)
        {
            return "";
        }
    }

    private List<String> arrayToList(JsonNode node)
    {
        List<String> list = new ArrayList<>();

        if(node != null && node.isArray())
        {
            for(JsonNode n : node)
            {
                list.add(n.asText(""));
            }
        }

        return list;
    }

    private Integer extractYear(String publishedDate)
    {
        if(publishedDate == null || publishedDate.length() < 4)
        {
            return null;
        }

        try
        {
            return Integer.parseInt(publishedDate.substring(0, 4));
        }
        catch(NumberFormatException e)
        {
            return null;
        }
    }
}