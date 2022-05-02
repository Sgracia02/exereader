package com.example.exereader.ui.creditos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.example.exereader.R;

import java.util.Locale;

/** CREDITOS */
public class CreditosFragment extends Fragment {
    private WebView web;
    private String idioma;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_creditos, container, false);
        web = view.findViewById(R.id.web);
        web.setWebViewClient(new WebViewClient());

        idioma =  Locale.getDefault().getLanguage(); // es
        if(!idioma.equalsIgnoreCase("es")){
            web.loadUrl("file:///android_asset/creditos_en.html");
        }else{
            web.loadUrl("file:///android_asset/creditos.html");
        }
        return view;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.findItem(R.id.ordenar).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }
}