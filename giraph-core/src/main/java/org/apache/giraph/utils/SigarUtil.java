package org.apache.giraph.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.hyperic.sigar.Sigar;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by 11363 on 5/31/2017.
 */
public class SigarUtil {
    public static Sigar getSigar(Mapper<?, ?, ?, ?>.Context context) throws IOException{
        final Logger LOG = Logger.getLogger(SigarUtil.class);

        String userHome = System.getProperty("user.home");
        String sigarFolderName = userHome + "/sigar_lib";
        File sigarFolder = new File(sigarFolderName);

        if(!sigarFolder.exists()){
            sigarFolder.mkdir();

            Configuration conf = context.getConfiguration();
            FileSystem fileSystem = FileSystem.get(conf);
            Path path = new Path("/libraries/sigar_lib.zip");
            if(!fileSystem.exists(path)){
                LOG.info(path.toString() + " does not exist!");
                return null;
            }

            InputStream in = fileSystem.open(path);
            File file = new File(sigarFolderName + "/sigar_lib.zip");
            if(!file.exists()){
                file.createNewFile();
            }
            OutputStream out = new FileOutputStream(file.getCanonicalFile());
            IOUtils.copyBytes(in, out, 4096, true);

            ZipFile zipFile = new ZipFile(file);
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if(LOG.isInfoEnabled()) {
                    LOG.info("decompress file :" + entry.getName());
                }
                File outFile = new File(sigarFolderName + "/" + entry.getName());
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));

                byte[] buffer = new byte[1024];
                while (true) {
                    int len = bis.read(buffer);
                    if (len == -1)
                        break;
                    bos.write(buffer, 0, len);
                }
                bis.close();
                bos.close();
            }
            zis.close();
            file.delete();
        }


        String seperator;
        if (OsCheck.getOperatingSystemType() == OsCheck.OSType.Windows)
            seperator = ";";
        else
            seperator = ":";
        String path = System.getProperty("java.library.path") + seperator + sigarFolder.getCanonicalPath();
        System.setProperty("java.library.path", path);

        if(LOG.isInfoEnabled()) {
            LOG.info("SigarUtil: After set java.library.path, it is " + System.getProperty("java.library.path"));
            LOG.info("SigarUtil: Successfully add the necessary native libraries that sigar needs.");
        }

        return new Sigar();
    }
}
