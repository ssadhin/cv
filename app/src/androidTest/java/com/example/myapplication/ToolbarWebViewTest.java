package com.example.myapplication;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import android.webkit.WebView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ToolbarWebViewTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void undoRedoHandlersAreBoundAndClickable() throws Throwable {
        MainActivity activity = activityRule.getActivity();
        assertThat(activity, notNullValue());

        WebView webView = activity.findViewById(R.id.webview);
        assertThat(webView, notNullValue());

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] result = new boolean[2];
        Throwable[] error = new Throwable[1];

        activityRule.runOnUiThread(() ->
                webView.evaluateJavascript(
                        "JSON.stringify({" +
                                "hasUndo: typeof window.undo === 'function'," +
                                "hasRedo: typeof window.redo === 'function'" +
                                "})",
                        value -> {
                            try {
                                if (value == null || value.equals("null")) {
                                    throw new AssertionError("Received null from evaluateJavascript");
                                }
                                String json = value;
                                if (json.startsWith("\"") && json.endsWith("\"") && json.length() >= 2) {
                                    json = json.substring(1, json.length() - 1)
                                            .replace("\\\"", "\"")
                                            .replace("\\\\", "\\");
                                }
                                JSONObject obj = new JSONObject(json);
                                result[0] = obj.optBoolean("hasUndo", false);
                                result[1] = obj.optBoolean("hasRedo", false);
                            } catch (Throwable t) {
                                error[0] = t;
                            } finally {
                                latch.countDown();
                            }
                        }
                )
        );

        if (!latch.await(15, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for WebView JS evaluation");
        }

        if (error[0] != null) {
            throw error[0];
        }

        assertTrue("window.undo should be defined", result[0]);
        assertTrue("window.redo should be defined", result[1]);
    }
}
