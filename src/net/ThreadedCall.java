package net;

import javafx.concurrent.Task;
import main.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class ThreadedCall<T> {


    private final String address;
    private final boolean authenticate;
    private final PostRequest[] posts;

    public ThreadedCall(String address, boolean authenticate, PostRequest... posts) {
        this.address = address;
        this.authenticate = authenticate;
        this.posts = posts;
    }

    private BufferedReader call() {
        try {
            //post data
            StringBuilder data = new StringBuilder(authenticate ? "id=" + User.active().getUniqueID() + " &username=" + User.active().getUsername() + "&password=" + Net.urlEncode(User.active().getPassword()) : "");
            boolean b = false;
            for (PostRequest request :  posts) {
                if (b)
                    data.append('&');
                data.append(request.toString());
                b = true;
            }
            byte[] postData = data.toString().getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            //start connection
            URL url = new URL(Net.root(), address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }
            return new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void threadedCall(Function<ArrayList<String>, T> action, Consumer<T> workWithValue) {
        Task<T> t = getOutput(action);
        t.setOnSucceeded(event -> workWithValue.accept(t.getValue()));
    }

    private Task<T> getOutput(Function<ArrayList<String>, T> action) {
        Task<T> call = new Task<>() {
            @Override
            protected T call() throws Exception {
                BufferedReader output = ThreadedCall.this.call();
                ArrayList<String> lines = new ArrayList<>();
                if (output == null)
                    return null;
                String s = output.readLine();
                while (s != null) {
                    lines.add(s);
                    s = output.readLine();
                }
                return action.apply(lines);
            }
        };
        Thread thread = new Thread(call);
        thread.start();
        return call;
    }
}
