package com.example.bernard.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PathEffect;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView image;
    Button btnA;
    Button btnB;
    Button btnC;
    Button btnD;

    Map<String, String> namesLinksMap;
    List<String> listNames;
    List<String> listLinks;
    int numberOfCelebs;

    int[] answers = {0, 0, 0, 0};

    // class for getting url content in background
    protected class DownloadWebContentTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                String retVal = "";

                // getting connection
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // getting input stream from url
                InputStream in = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                Log.i("STRINGSSSS", "Getting stream");

                // reading stream char by char and adding it to the end of retVal string
                int intChar = reader.read();
                while (intChar != -1) {
                    retVal += (char) intChar;
                    intChar = reader.read();
                }
                Log.i("STRINGSSSS", retVal);
                // returning retVal
                return retVal;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // class for getting images from WEB in background
    protected class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                // iterating through urls and fetching images
                Log.i("STRINGSSSS", "Getting images");
                    // getting connection
                    URL url = new URL(urls[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();


                    InputStream in = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    return bmp;
            } catch (Exception e) {
                return Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String html;
        // fetch web content
        try {
            DownloadWebContentTask task = new DownloadWebContentTask();
            html = task.execute("http://www.posh24.se/kandisar").get();
            if (html == null) throw new Exception();


            // get only strings inside img tag
            Pattern pattern = Pattern.compile("<img src=\"(.*?)>");
            Matcher m = pattern.matcher(html);

            List<String> list = new ArrayList<String>();

            while (m.find()) {
                list.add(m.group(1));
            }

            // get names and links of celeb's
            listNames = getListByPatern(list, "alt=\"(.*?)\"");
            listLinks = getListByPatern(list, "(.*?)\" alt");
            numberOfCelebs = listNames.size();

            String[] links = new String[numberOfCelebs];
            for (int i = 0; i < listLinks.size(); i++) {
                links[i] = listLinks.get(i);
            }

            // combine lists into map
            namesLinksMap = new LinkedHashMap<String, String>();
            for (int i = 0; i < listNames.size(); i++) {
                namesLinksMap.put(listNames.get(i), listLinks.get(i));
            }

            // remove initial textView
            TextView text = (TextView) findViewById(R.id.textWait);
            text.setVisibility(View.INVISIBLE);

            // instatiate buttons and image
            image = (ImageView) findViewById(R.id.celebrityImage);
            btnA = (Button) findViewById(R.id.btnA);
            btnB = (Button) findViewById(R.id.btnB);
            btnC = (Button) findViewById(R.id.btnC);
            btnD = (Button) findViewById(R.id.btnD);

            // set new question
            setNewQuestion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getListByPatern(List<String> source, String pattern) {
        List<String> list = new ArrayList<String>();

        for (String s:
                source) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(s);

            while (m.find()) {
                list.add(m.group(1));
            }
        }

        return list;
    }

    private void setNewQuestion() {
        // set all tags
        btnA.setTag(0);
        btnB.setTag(0);
        btnC.setTag(0);
        btnD.setTag(0);

        // set empty texts
        btnA.setText("");
        btnB.setText("");
        btnC.setText("");
        btnD.setText("");

        // get random celebrity
        Random r = new Random();
        int indexOfCeleb = r.nextInt(numberOfCelebs);

        String rightAnswerName = listNames.get(indexOfCeleb);

        // get bitmap
        Bitmap bmp;
        try {
            DownloadImageTask imageTask = new DownloadImageTask();
            bmp = imageTask.execute(namesLinksMap.get(rightAnswerName)).get();
        } catch (Exception e){
            bmp = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        }
        image.setImageBitmap(bmp);

        // choose right answer button
        int rightAnswerButtonInt = r.nextInt(4);
        switch (rightAnswerButtonInt) {
            case 0:
                btnA.setTag(1);
                btnA.setText(listNames.get(indexOfCeleb));
                break;
            case 1:
                btnB.setTag(1);
                btnB.setText(listNames.get(indexOfCeleb));
                break;
            case 2:
                btnC.setTag(1);
                btnC.setText(listNames.get(indexOfCeleb));
                break;
            default:
                btnD.setTag(1);
                btnD.setText(listNames.get(indexOfCeleb));
                break;
        }

        int firstWrongAnswer = -1;
        int secondWrongAnswer = -1;

        int counter = 3;
        while (counter > 0) {
            int wrongAnswerInt = r.nextInt(numberOfCelebs);
            if (wrongAnswerInt == indexOfCeleb) continue;

            if (firstWrongAnswer == -1) {
                firstWrongAnswer = wrongAnswerInt;
            } else if (secondWrongAnswer == -1 && wrongAnswerInt != firstWrongAnswer) {
                secondWrongAnswer = wrongAnswerInt;
            } else if (wrongAnswerInt == secondWrongAnswer || wrongAnswerInt == firstWrongAnswer){
                continue;
            } else {
                // do nothing
            }

            if (btnA.getText().equals("")) {
                btnA.setText(listNames.get(wrongAnswerInt));
            } else if (btnB.getText().equals("")) {
                btnB.setText(listNames.get(wrongAnswerInt));
            } else if (btnC.getText().equals("")) {
                btnC.setText(listNames.get(wrongAnswerInt));
            } else {
                btnD.setText(listNames.get(wrongAnswerInt));
            }


            counter--;
        }

        btnA.setVisibility(View.VISIBLE);
        btnB.setVisibility(View.VISIBLE);
        btnC.setVisibility(View.VISIBLE);
        btnD.setVisibility(View.VISIBLE);

        image.setVisibility(View.VISIBLE);
    }

    public void answerClick (View view) {
        Button b = (Button) view;
        if (Integer.parseInt(b.getTag().toString()) == 1) {
            Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong", Toast.LENGTH_SHORT).show();
        }

        setNewQuestion();
    }
}
