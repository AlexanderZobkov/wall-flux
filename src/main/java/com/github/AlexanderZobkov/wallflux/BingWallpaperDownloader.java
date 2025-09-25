package com.github.AlexanderZobkov.wallflux;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class BingWallpaperDownloader {

    /**
     * Gets info on a picture of the day.
     *
     * @return descriptor when necessary information.
     * @throws IOException when any error occurred while getting info on picture of the day.
     */
    public WallpaperDescriptor getPictureOfTheDayDescriptor() throws IOException {
        // https://dev.to/excalibra/entry-level-bing-wallpaper-scraper-7pg
        // https://github.com/grubersjoe/bing-daily-photo
        // https://github.com/gsdukbh/wallpaper/blob/main/src/main/java/top/werls/wallpaper/App.java

        final String apiUrl = "https://www.bing.com/HPImageArchive.aspx?"
                + "format=js"
                + "&idx=0"
                + "&n=1"
                + "&mkt=" + Locale.getDefault().toLanguageTag()
                + "&uhd=1"
                + "&uhdwidth=2560"
                + "&uhdheight=1440";

        System.out.println("Requesting image of the day: " + apiUrl);
        // As of this moment, SOCKS proxies are not supported by native HttpClient: bugs.openjdk.org/JDK-8214516
        final HttpURLConnection connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
        connection.setRequestMethod("GET");
        // Just in case, try to pretend we are a browser.
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/139.0.0.0 Safari/537.36");

        // Step 2: Read API response
        final StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        // Step 3: Parse the JSON response to get the imageUrl URL
        final JSONObject jsonResponse = new JSONObject(response.toString());
        System.out.println("Got the image: \n" + jsonResponse.toString(2));
        final JSONArray images = jsonResponse.getJSONArray("images");
        final JSONObject firstImage = images.getJSONObject(0);
        final String imageUrl = "https://www.bing.com" + firstImage.getString("url");
        final String imageCopyright = firstImage.getString("copyright");
        final String imageCopyrightLink = firstImage.getString("copyrightlink");

        return new WallpaperDescriptor(imageCopyright,
                URI.create(imageUrl).toURL(),
                URI.create(imageCopyrightLink).toURL());
    }

    /**
     * Downloads a requested imageUrl.
     *
     * @param imageURL URL where imageUrl is located.
     * @return absolute or relative path to the downloaded image.
     * @throws IOException when any error occurred while downloading the requested imageUrl.
     */
    public File download(final URL imageURL) throws IOException {
        final File downloadPath = Path.of(System.getProperty("user.home"),
                "Pictures",
                "BingWallpapers").toFile();
        if (!downloadPath.exists()) {
            downloadPath.mkdirs();
        }

        final String fileName = "BingWall.jpg";
        final InputStream inStream = imageURL.openConnection().getInputStream();
        final File output = new File(downloadPath, fileName);
        final OutputStream fileOutStream = Files.newOutputStream(output.toPath());

        final byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) != -1) {
            fileOutStream.write(buffer, 0, bytesRead);
        }
        inStream.close();
        fileOutStream.close();

        System.out.println("Wallpaper downloaded to: " + output.getAbsolutePath());
        return output;
    }
}
