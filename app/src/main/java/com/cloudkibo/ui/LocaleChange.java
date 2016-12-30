package com.cloudkibo.ui;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.library.LocaleHelper;

public class LocaleChange extends Activity implements View.OnClickListener {

    TextView languageLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locale_change);

        languageLabel = (TextView) findViewById(R.id.textView4);
    }


    public void onDefaultClick() {
        LocaleHelper.setDefaultLocale(this);
        updateViews();
    }

    public void onEnglishClick() {
        LocaleHelper.setLocale(this, "en");
        updateViews();
    }

    public void onUrduClick() {
        LocaleHelper.setLocale(this, "ur");
        updateViews();
    }

    public void onArabicClick() {
        LocaleHelper.setLocale(this, "ar");
        updateViews();
    }

    public void onFrenchClick() {
        LocaleHelper.setLocale(this, "fr");
        updateViews();
    }

    public void onSpanishClick() {
        LocaleHelper.setLocale(this, "es");
        updateViews();
    }

    private void updateViews() {
        // if you want you just call activity to restart itself to redraw all the widgets with the correct locale
        // however, it will cause a bad look and feel for your users
        //
        // this.recreate();

        //or you can just update the visible text on your current layout
        Resources resources = getResources();

        languageLabel.setText(resources.getString(R.string.language));

        ((TextView) findViewById(R.id.buttonEnglish)).setText(resources.getString(R.string.lang_english));
        ((TextView) findViewById(R.id.buttonUrdu)).setText(resources.getString(R.string.lang_urdu));
        ((TextView) findViewById(R.id.buttonArabic)).setText(resources.getString(R.string.lang_arabic));
        ((TextView) findViewById(R.id.buttonFrench)).setText(resources.getString(R.string.lang_french));
        ((TextView) findViewById(R.id.buttonSpanish)).setText(resources.getString(R.string.lang_spanish));
        ((TextView) findViewById(R.id.buttonDefault)).setText(resources.getString(R.string.common_default));

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.buttonEnglish){
            onEnglishClick();
        } else if(view.getId() == R.id.buttonUrdu){
            onUrduClick();
        } else if(view.getId() == R.id.buttonArabic){
            onArabicClick();
        } else if(view.getId() == R.id.buttonFrench){
            onFrenchClick();
        } else if(view.getId() == R.id.buttonSpanish){
            onSpanishClick();
        } else if(view.getId() == R.id.buttonDefault){
            onDefaultClick();
        }
    }
}
