package com.mojang.minecraftpe;

import android.util.Log;
import android.util.Pair;
import com.braze.Constants;
import com.google.common.net.HttpHeaders;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CrashManager {
    private String mCrashDumpFolder;
    private String mCrashUploadURI;
    private String mCrashUploadURIWithSentryKey;
    private String mCurrentSessionId;
    private String mExceptionUploadURI;
    private Thread.UncaughtExceptionHandler mPreviousUncaughtExceptionHandler = null;

    private static native String nativeNotifyUncaughtException();

    public CrashManager(String str, String str2, SentryEndpointConfig sentryEndpointConfig) {
        this.mCrashUploadURI = null;
        this.mCrashUploadURIWithSentryKey = null;
        this.mExceptionUploadURI = null;
        this.mCrashDumpFolder = str;
        this.mCurrentSessionId = str2;
        this.mCrashUploadURI = sentryEndpointConfig.url + "/api/" + sentryEndpointConfig.projectId + "/minidump/";
        this.mCrashUploadURIWithSentryKey = this.mCrashUploadURI + "?sentry_key=" + sentryEndpointConfig.publicKey;
        this.mExceptionUploadURI = sentryEndpointConfig.url + "/api/" + sentryEndpointConfig.projectId + "/store/?sentry_version=7&sentry_key=" + sentryEndpointConfig.publicKey;
    }

    public void installGlobalExceptionHandler() {
        this.mPreviousUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable th) {
                CrashManager.this.handleUncaughtException(thread, th);
            }
        });
    }

    public String getCrashUploadURI() {
        return this.mCrashUploadURI;
    }

    public String getExceptionUploadURI() {
        return this.mExceptionUploadURI;
    }

    private void handleUncaughtException(Thread thread, Throwable th) {
        Thread.setDefaultUncaughtExceptionHandler(this.mPreviousUncaughtExceptionHandler);
        Log.e("MCPE", "In handleUncaughtException()");
        try {
            JSONObject jSONObject = new JSONObject(nativeNotifyUncaughtException());
            Object replaceAll = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Object format = simpleDateFormat.format(new Date());
            jSONObject.put("event_id", replaceAll);
            jSONObject.put("timestamp", format);
            jSONObject.put("logger", "na");
            jSONObject.put("platform", "java");
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("type", th.getClass().getName());
            jSONObject2.put("value", th.getMessage());
            JSONObject jSONObject3 = new JSONObject();
            JSONArray jSONArray = new JSONArray();
            StackTraceElement[] stackTrace = th.getStackTrace();
            for (int length = stackTrace.length - 1; length >= 0; length--) {
                StackTraceElement stackTraceElement = stackTrace[length];
                JSONObject jSONObject4 = new JSONObject();
                jSONObject4.put("filename", stackTraceElement.getFileName());
                jSONObject4.put("function", stackTraceElement.getMethodName());
                jSONObject4.put("module", stackTraceElement.getClassName());
                jSONObject4.put("in_app", stackTraceElement.getClassName().startsWith("com.mojang"));
                if (stackTraceElement.getLineNumber() > 0) {
                    jSONObject4.put("lineno", stackTraceElement.getLineNumber());
                }
                jSONArray.put(jSONObject4);
            }
            jSONObject3.put("frames", jSONArray);
            jSONObject2.put("stacktrace", jSONObject3);
            jSONObject.put("exception", jSONObject2);
            String str = this.mCrashDumpFolder + "/" + this.mCurrentSessionId + ".except";
            Log.d("MCPE", "CrashManager: Writing unhandled exception information to: " + str);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(str));
            outputStreamWriter.write(jSONObject.toString(4));
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("MCPE", "IO exception: " + e.toString());
        } catch (JSONException e2) {
            Log.e("MCPE", "JSON exception: " + e2.toString());
        }
        this.mPreviousUncaughtExceptionHandler.uncaughtException(thread, th);
    }

    private Pair<HttpResponse, String> uploadException(File file) {
        String message;
        HttpResponse httpResponse = null;
        try {
            Log.i("MCPE", "CrashManager: reading exception file at " + file.getPath());
            String str = "";
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                str = str + readLine + "\n";
            }
            Log.i("MCPE", "Sending exception by HTTP to " + this.mExceptionUploadURI);
            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(this.mExceptionUploadURI);
            httpPost.setEntity(new StringEntity(str));
            httpResponse = defaultHttpClient.execute(httpPost);
            message = null;
        } catch (Exception e) {
            Log.w("MCPE", "CrashManager: Error uploading exception: " + e.toString());
            e.printStackTrace();
            message = e.getMessage();
        }
        return new Pair<>(httpResponse, message);
    }

    private String uploadCrashFile(String str, String str2, String str3) {
        Pair<HttpResponse, String> uploadException;
        String str4 = "";
        String str5;
        File file = new File(str);
        int i = 3;
        String str6 = null;
//        while (true) {
//            if (str.endsWith(".dmp")) {
//                uploadException = uploadDump(file, this.mCrashUploadURIWithSentryKey, str2, str3);
//            } else {
//                uploadException = uploadException(file);
//            }
//            if (uploadException.first != null) {
//                int statusCode = ((HttpResponse) uploadException.first).getStatusLine().getStatusCode();
//                if (statusCode == 200) {
//                    Log.i("MCPE", "Successfully uploaded dump file " + str);
//                    return null;
//                }
//                if (statusCode == 429) {
//                    Header firstHeader = ((HttpResponse) uploadException.first).getFirstHeader(HttpHeaders.RETRY_AFTER);
//                    if (firstHeader != null) {
//                        Log.w("MCPE", "Received Too Many Requests response, retrying after " + Integer.parseInt(firstHeader.getValue()) + Constants.BRAZE_PUSH_SUMMARY_TEXT_KEY);
//                        try {
//                            Thread.sleep(r4 * 1000);
//                        } catch (InterruptedException unused) {
//                        }
//                        str5 = str6;
//                        str4 = null;
//                    } else {
//                        Log.w("MCPE", "Received Too Many Requests response with no Retry-After header, so dropping event " + str);
//                        return "TooManyRequestsNoRetryAfter";
//                    }
//                } else {
//                    str4 = "Unrecognied HTTP response: \"" + ((HttpResponse) uploadException.first).getStatusLine() + "\"";
//                    str5 = "HTTP: " + ((HttpResponse) uploadException.first).getStatusLine().toString();
//                }
//            } else {
//                str4 = "An error occurred uploading an event:\"" + ((String) uploadException.second) + "\"";
//                str5 = (String) uploadException.second;
//            }
//            if (str4 != null) {
//                int i2 = i - 1;
//                if (i > 0) {
//                    Log.e("MCPE", str4 + "; retrying event " + str + " " + i2 + " more time(s) after 1000 ms");
//                    try {
//                        Thread.sleep(1000L);
//                    } catch (InterruptedException unused2) {
//                    }
//                    i = i2;
//                } else {
//                    Log.e("MCPE", str4 + "; dropping event " + str);
//                    return str5;
//                }
//            }
//            str6 = str5;
//        }
        return str4;
    }

    private static Pair<HttpResponse, String> uploadDump(File file, String str, String str2, String str3) {
        Object obj = null;
        Object obj2 = null;
        try {
            Log.i("MCPE", "CrashManager: uploading " + file.getPath());
            Log.d("MCPE", "CrashManager: sentry parameters: " + str3);
            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(str);
            MultipartEntity multipartEntity = new MultipartEntity();
            multipartEntity.addPart("upload_file_minidump", new FileBody(file));
            multipartEntity.addPart("sentry", new StringBody(str3));
            httpPost.setEntity(multipartEntity);
            obj = defaultHttpClient.execute(httpPost);
            try {
                Log.d("MCPE", "CrashManager: Executed dump file upload with no exception: " + file.getPath());
            } catch (Exception e) {
                e = e;
                obj2 = obj;
                Log.w("MCPE", "CrashManager: Error uploading dump file: " + file.getPath());
                e.printStackTrace();
                obj = obj2;
                obj2 = e.getMessage();
                return new Pair(obj, obj2);
            }
        } catch (Exception e2) {

        }
        return new Pair(obj, obj2);
    }
}
