import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUploader extends AsyncTask<String, Integer, Boolean> {

    private static final int BUFFER_SIZE = 4096;
    private File file;
    private String uploadUrl;

    public FileUploader(File file, String uploadUrl) {
        this.file = file;
        this.uploadUrl = uploadUrl;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            URL url = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("file", file.getName());

            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write((twoHyphens + boundary + lineEnd).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\";filename=\"" + file.getName() + "\"" + lineEnd).getBytes());
            outputStream.write(lineEnd.getBytes());

            InputStream inputStream = new BufferedInputStream(fileInputStream);
            buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.write(lineEnd.getBytes());
            outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}