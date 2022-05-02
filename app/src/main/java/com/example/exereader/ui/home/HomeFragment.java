package com.example.exereader.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exereader.Adaptador;
import com.example.exereader.ClaseSharedPreferences;
import com.example.exereader.ui.FileChooser;
import com.example.exereader.ui.FragmentListaVacia;
import com.example.exereader.Proyectos;
import com.example.exereader.R;
import com.example.exereader.ui.FragmentWebview;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class HomeFragment extends Fragment{
    private RecyclerView proyectos;
    private Adaptador adaptador;
    private ArrayList<Proyectos> lista;
    private Button buttonHome;
    String titulo="", autor="";
    private static final int REQUEST_PERMISSION_CODE = 5656;

    public boolean menuOrdenar;
    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuOrdenar=true;
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //Hacemos visible la AppBar
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        //Eliminamos los valores con los que trabajamos
        ClaseSharedPreferences.eliminarDatos(getContext(),"directorio");
        ClaseSharedPreferences.eliminarDatos(getContext(),"archivo");
        ClaseSharedPreferences.eliminarDatos(getContext(),"editable");
        ClaseSharedPreferences.eliminarDatos(getContext(),"cambio");
        ClaseSharedPreferences.eliminarDatos(getContext(),"Uri");
        ClaseSharedPreferences.eliminarDatos(getContext(),"tp");

        String uri = ClaseSharedPreferences.verDatos(getContext(), "uriDefault");

        //Bloqueamos las opciones de sobreProyecto
        setHasOptionsMenu(true);
        buttonHome = root.findViewById(R.id.floatingActionButton);
        proyectos = root.findViewById(R.id.proyectos);
        proyectos.setLayoutManager(new LinearLayoutManager(getContext()));
        lista = new ArrayList<>();

        leerDirectorio();

        if(lista.size() > 0){
            adaptador = new Adaptador(lista, ((AppCompatActivity) getActivity()));
            adaptador.cambiarOrden("date");
            proyectos.setAdapter(adaptador);
        }

        buttonHome.setOnClickListener(v -> verificarPermisos());

        if(!uri.equalsIgnoreCase(" ")){
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FileChooser fileChooser = FileChooser.newInstance("", "");
            fragmentTransaction.replace(R.id.nav_host_fragment, fileChooser);
            fragmentTransaction.commit();
        }

        //Si no hay ningún archivo mostramos la pantalla de "No hay contenido"
        if(lista.size() == 0){
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentListaVacia fragmentListaVacia = FragmentListaVacia.newInstance("","");
            menuOrdenar=false;
            fragmentTransaction.replace(R.id.nav_host_fragment, fragmentListaVacia);
            fragmentTransaction.commit();
        }
        return root;
    }

    /*@Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.findItem(R.id.ordenar).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }*/

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if(menuOrdenar){
            menu.findItem(R.id.ordenar).setVisible(true);
        }else {
            menu.findItem(R.id.ordenar).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.ordFecha:
                Toast.makeText(getContext(),"opcion fecha",Toast.LENGTH_LONG).show();
                adaptador.cambiarOrden("date");
                proyectos.setAdapter(adaptador);
                break;
            case R.id.ordAutor:
                Toast.makeText(getContext(),"opcion autor",Toast.LENGTH_LONG).show();
                adaptador.cambiarOrden("author");
                proyectos.setAdapter(adaptador);
                break;
            case R.id.ordTitulo:
                Toast.makeText(getContext(),"opcion titulo",Toast.LENGTH_LONG).show();
                adaptador.cambiarOrden("title");
                proyectos.setAdapter(adaptador);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Método que comprueba si ya hemos aceptado los permisos para poder seleccionar el archivo.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        String idioma =  Locale.getDefault().getLanguage(); // es

        if (requestCode == REQUEST_PERMISSION_CODE) {//Este caso se ejecutaría si el usuario cancela los permisos
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Si tenemos permisos pasamos a lanzar el selector de archivos (fileChooser)
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FileChooser fileChooser = FileChooser.newInstance(null, "");
                fragmentTransaction.replace(R.id.nav_host_fragment, fileChooser);
                fragmentTransaction.commit();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                if(!idioma.equalsIgnoreCase("es")) {
                    builder.setMessage("In order to unzip a file you must accept the permissions").setTitle("Error")
                            .setPositiveButton("Ok", (dialogInterface, which) -> dialogInterface.cancel());
                }else{
                    builder.setMessage("Para poder descomprimir un archivo debe aceptar los permisos.").setTitle("Error")
                            .setPositiveButton("Ok", (dialogInterface, which) -> dialogInterface.cancel());
                }
                builder.show();
            }
        }
    }

    /**
     * Método que verfica los permisos de escritura y lectura para poder
     * seleccionar el archivo.
     * Se ejecuta la primera vez que instalamos la aplicación.
     */
    private void verificarPermisos() {
        //Obtenemos los permisos de escritura.
        int permission = checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //SI no tenemos permisos los solicita por pantalla al usuario.
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET},
                    REQUEST_PERMISSION_CODE);
        }else{
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FileChooser fileChooser = FileChooser.newInstance("","");
            fileChooser.hideMenu=true;
            fragmentTransaction.replace(R.id.nav_host_fragment, fileChooser);
            fragmentTransaction.commit();
        }
    }

    /** Método para leer el directorio files de la aplicación*/
    private void leerDirectorio(){
        File xmlDatos;
        String path = getContext().getExternalFilesDir(null).toString();
        File carpetaFicheros = new File(path);
        File[] files = carpetaFicheros.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()){
                File[] fileBuscar = files[i].listFiles();
                for(int x=0; x < fileBuscar.length; x++){
                    if (fileBuscar[x].getName().equals("contentv3.xml")) {
                        xmlDatos = fileBuscar[x];
                        if(xmlDatos.exists()){
                            leerArchivo(xmlDatos);
                            Proyectos proyectos = new Proyectos(buscarImagen(files[i]),titulo,autor,files[i].lastModified(), ClaseSharedPreferences.verDatos(getContext(),"Uri"),files[i].getName());
                            lista.add(proyectos);
                            titulo="";
                            autor="";
                        }
                    }
                }
            }
        }
    }


    /** Método para buscar una imagen .png o .jpg para la lista de archivos recientes*/
    private String buscarImagen(File file) {
        File[] archivos = file.listFiles();
        String ruta = null;
        int i = 0,x=0;
        boolean portada = false;

        while(ruta == null && x < archivos.length){
            if(archivos[x].getName().contains(".png") || archivos[x].getName().contains(".jpg")){
                if(!archivos[x].getAbsolutePath().contains("icon_") && !archivos[x].getAbsolutePath().contains("popup_bg")
                        && !archivos[x].getAbsolutePath().contains("licenses") && archivos[x].getAbsolutePath().contains("portada")){
                    ruta = archivos[x].getAbsolutePath();
                    portada=true;
                }
            }
            x++;
        }


        if(portada==false) {
            while (ruta == null && i < archivos.length) {
                if (archivos[i].getName().contains(".png") || archivos[i].getName().contains(".jpg")) {
                    if (!archivos[i].getAbsolutePath().contains("icon_") && !archivos[i].getAbsolutePath().contains("popup_bg")
                            && !archivos[i].getAbsolutePath().contains("licenses") && !archivos[i].getAbsolutePath().contains("88x31")) {
                        ruta = archivos[i].getAbsolutePath();
                    }
                }
                i++;
            }
        }
        return ruta;
    }
    /** Método para leer los archivos de configuración y así poder mostrar el título
    * y el nombre del autor en la lista de archivos recientes */
    public void leerArchivo(File xmlDatos) {
        try {
            DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentB = db.newDocumentBuilder();
            Document doc = documentB.parse(new File(xmlDatos.getAbsolutePath()));

            NodeList nList = doc.getElementsByTagName("dictionary");
            for (int j = 0; j < nList.getLength(); j++) {
                NodeList nListString = nList.item(j).getChildNodes();
                if (nListString.getLength() > 0) {
                    for (int i = 0; i < nListString.getLength(); i++) {
                        Node nNode = nListString.item(i);

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;

                            if (eElement.getAttribute("value").equals("_title") && titulo.equals("")) {
                                Node nodoSiguiente = nListString.item(i + 2);
                                Element eElementSiguiente = (Element) nodoSiguiente;
                                titulo = eElementSiguiente.getAttribute("value");
                            }

                            if (eElement.getAttribute("value").equals("_author") && autor.equals("")) {
                                Node nodoSiguiente = nListString.item(i + 2);
                                Element eElementSiguiente = (Element) nodoSiguiente;
                                autor = eElementSiguiente.getAttribute("value");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}