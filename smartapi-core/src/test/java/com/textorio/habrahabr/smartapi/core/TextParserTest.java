package com.textorio.habrahabr.smartapi.core;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;

public class TextParserTest {

    @Test
    public void test () throws UnirestException {
        List<String> inputs = getResourceFileAsList("transcript.txt");
        List<String> founds = new ArrayList<>();

        Pattern HHMMSS = Pattern.compile("\\d+:\\d+:\\d+");
        Pattern MMSS = Pattern.compile("\\d+:\\d+");
        //Pattern SS = Pattern.compile("\\d+");//Секунды обязательно в квадратных скобках!
        List<Pattern> patterns = Arrays.asList(new Pattern[]{HHMMSS,MMSS});
        Matcher matcher;

        for (String input: inputs) {
            for (Pattern pattern: patterns) {
                matcher = pattern.matcher(input);
                while (matcher.find()) {
                    String group = matcher.group(0);
                    founds.add(group);
                }
            }
        }

        List<String> works = new ArrayList<>();
        for (String found: founds) {
            String[] components = found.split(":");
            String time = null;
            if (components.length == 3) {
                int hourSecs = 3600 * Integer.parseInt(components[0]);
                int minSecs = 60 * Integer.parseInt(components[1]);
                int secs = Integer.parseInt(components[2]);
                int sumSecs = hourSecs + minSecs + secs;
                time = Integer.toString(sumSecs);
            } else if (components.length == 2) {
                int minSecs = 60 * Integer.parseInt(components[0]);
                int secs = Integer.parseInt(components[1]);
                int sumSecs = minSecs + secs;
                time = Integer.toString(sumSecs);
            } else {
                System.out.println("unsupported time format");
            }
            works.add(time);
            System.out.println(time);
        }

        String[] result = new String[works.size()];
        result = works.toArray(result);
    }

    public String getResourceFileAsString(String fileName) {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getResourceFileAsList(String fileName) {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.lines().collect(Collectors.toList());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
