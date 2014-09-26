package com.multi.assetsencoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Core {

    public static void doWork(String jsonfile, String dir, String outputDir) throws IOException, ParseException { //говнокод
        System.out.println("Started.");
        FileReader reader = new FileReader(jsonfile);
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = (JSONObject) ((JSONObject) jsonParser.parse(reader)).get("objects");

        File assetsDir = new File(dir);
        List<File> fileList = new ArrayList<File>();
        boolean somethingFound = false;
        if (assetsDir.isDirectory()) {
            fileList = getFileListRecursive(assetsDir);
        }
        System.out.println("Assets dir is not dir or not exists =O");


        for (File f : fileList) {
            String path = getRelativePath(f, assetsDir, true);
            if (obj.containsKey(path)) {
                String hash = (((JSONObject) obj.get(path)).get("hash")).toString();
                String hashDir = hash.substring(0, 2);
                System.out.println("Found [" + path + "] in JSON, hash is [" + hash + "], dir is [" + hashDir + "]");
                somethingFound = true;
                File hashOutput = new File(outputDir, hashDir + File.separator + hash);
                File hashOutputDir = new File(outputDir, hashDir);
                hashOutputDir.mkdirs();
                copyFile(f, hashOutput);
            }
        }
        System.out.println("Finished.");
        if (!somethingFound) System.out.println("Nothing found in json. Assets dir contains minecraft dir?");
    }


    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static String getRelativePath(File file, File folder, boolean slashes) {
        String filePath = file.getAbsolutePath();
        String folderPath = folder.getAbsolutePath();
        if (filePath.startsWith(folderPath)) {
            String s = filePath.substring(folderPath.length() + 1);
            if (slashes) s = s.replace('\\', '/');
            return s;
        } else {
            return null;
        }
    }

    public static List<File> getFileListRecursive(File dir) {
        List<File> list = new ArrayList<File>();
        File[] files = dir.listFiles();

        if (files != null)
            for (File file : files) {

                if (file.isDirectory()) {
                    list.addAll(getFileListRecursive(file));
                } else list.add(file);
            }

        return list;
    }


    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: jsonFile assetsDir outputDir");
            System.out.println("Usage example: java -jar AssetsEncoder.jar 1.8.json assets converted");
            System.out.println("THIS JAR MUST BE IN SAME PATH AS JSON FILE AND ASSETS DIR!");
            System.out.println("ASSETS DIR MUST CONTAIN \"minecraft\" DIR!");
            return;
        }
        try {
            doWork(args[0], args[1], args[2]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
