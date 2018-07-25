/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import it.unibo.participact.domain.local.ImageDescriptor;

public class ImageDescriptorUtility {

    private final static Logger logger = LoggerFactory.getLogger(ImageDescriptorUtility.class);

    private static final String IMAGE_DESCRIPTOR_EXTENSION = ".ids";

    public static synchronized boolean persistImageDescriptor(Context context, String fileName,
                                                              ImageDescriptor imageDescriptor) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            File file = new File(context.getExternalFilesDir(null), fileName);
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(imageDescriptor);
            objectOutputStream.close();
            return true;
        } catch (IOException e) {
            logger.error("Exception persisting ImageDescriptor {}.", fileName, e);
            return false;
        } finally {
            try {
                objectOutputStream.close();
                fileOutputStream.close();
            } catch (Exception ex) {
                logger.error("Exception persisting ImageDescriptor {}. Closing stream.", fileName, ex);
            }
        }
    }

    public static synchronized ImageDescriptor loadImageDescriptor(Context context, String fileName) {
        ImageDescriptor result = null;
        ObjectInputStream objectInputStream = null;
        FileInputStream fileInputStream = null;
        try {
            if (!fileName.endsWith(IMAGE_DESCRIPTOR_EXTENSION)) {
                fileName = fileName + IMAGE_DESCRIPTOR_EXTENSION;
            }
            File file = new File(context.getExternalFilesDir(null), fileName);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                objectInputStream = new ObjectInputStream(fileInputStream);
                Object obj = objectInputStream.readObject();
                objectInputStream.close();

                if (obj instanceof ImageDescriptor) {
                    result = (ImageDescriptor) obj;
                }
            }
        } catch (Exception e) {
            logger.error("Exception loading ImageDescriptor {}.", fileName, e);
        } finally {
            try {
                objectInputStream.close();
                fileInputStream.close();
            } catch (Exception ex) {
                logger.error("Exception loading ImageDescriptor {}. Closing stream.", fileName, ex);
            }
        }
        return result;
    }

    public static synchronized boolean deleteImageDescriptor(Context context, String fileName) {

        boolean result = false;
        try {
            if (!fileName.endsWith(IMAGE_DESCRIPTOR_EXTENSION)) {
                fileName = fileName + IMAGE_DESCRIPTOR_EXTENSION;
            }
            File file = new File(context.getExternalFilesDir(null), fileName);
            if (file.exists()) {
                result = file.delete();
            }
        } catch (Exception e) {
            logger.error("Exception deleting ImageDescriptor {}.", fileName, e);
        }
        return result;
    }

    public static synchronized boolean deleteImageDescriptorAndRelatedImage(Context context,
                                                                            String descriptorFileName) {

        boolean result = false;
        try {
            if (!descriptorFileName.endsWith(IMAGE_DESCRIPTOR_EXTENSION)) {
                descriptorFileName = descriptorFileName + IMAGE_DESCRIPTOR_EXTENSION;
            }
            ImageDescriptor imD = loadImageDescriptor(context, descriptorFileName);
            File file = new File(imD.getImagePath());
            if (file.exists()) {
                result = file.delete();
            }
            if (result) {
                result = result && deleteImageDescriptor(context, descriptorFileName);
            }
        } catch (Exception e) {
            logger.error("Exception deleting ImageDescriptor {} and related image.", descriptorFileName, e);
        }
        return result;
    }

    public static synchronized boolean renameFile(Context context, String oldFileName,
                                                  String newFileName) {

        boolean result = false;
        try {
            File file = new File(context.getExternalFilesDir(null), oldFileName);
            if (file.exists()) {
                result = file.renameTo(new File(context.getExternalFilesDir(null), newFileName));
            }
        } catch (Exception e) {
            logger.error("Exception renaming file {}.", oldFileName + " to " + newFileName, e);
        }
        return result;
    }

    public static synchronized File[] getImageDescriptors(Context context) {
        FilenameFilter fileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File file;
                if (filename.endsWith(IMAGE_DESCRIPTOR_EXTENSION)) {
                    return true;
                }
                if (filename.equalsIgnoreCase("logs"))
                    return false;
                file = new File(dir.getAbsolutePath() + "/" + filename);
                return file.isDirectory();
            }
        };

        File dir = new File(context.getExternalFilesDir(null).getAbsolutePath());
        return dir.listFiles(fileNameFilter);
    }
}
