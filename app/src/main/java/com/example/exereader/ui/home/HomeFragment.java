package com.example.exereader.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebViewFragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exereader.Adaptador;
import com.example.exereader.ClaseSharedPreferences;
import com.example.exereader.MainActivity;
import com.example.exereader.ui.FileChooser;
import com.example.exereader.ui.FileFromURL;
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

    private boolean editable;

    public boolean menuOrdenar;

    private String url = "";
    private long mFileDownloadedId;
    String idioma =  Locale.getDefault().getLanguage(); // es
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
            adaptador.cambiarOrden("fechaDesc");
            proyectos.setAdapter(adaptador);
        }

        buttonHome.setOnClickListener(v -> {
            modoAccederRecurso();
        });


        /*metodo para borrar elementos de la lista al deslizar*/
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            //al deslizar un componente de la lista da la opcion de borrar o no,
            // si seleccionamos la opcion no o pulsamos fuera del Dialog, vuelve a cargar la lista como estaba anteriormente
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String idioma =  Locale.getDefault().getLanguage();
                Proyectos proyecto = lista.get(viewHolder.getAdapterPosition());
                String path = getContext().getExternalFilesDir(null).toString();
                File carpetaFicheros = new File(path);
                File[] files = carpetaFicheros.listFiles();
                int pos = viewHolder.getAdapterPosition();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory() && proyecto.getNombrecarpeta().equalsIgnoreCase(files[i].getName())) {
                        int finalI1 = i;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        if(!idioma.equalsIgnoreCase("es")){
                            builder.setTitle("Confirmation");
                            builder.setMessage("Delete the contents of the application. Continue?");
                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                borrarApp(files[finalI1]);
                                ClaseSharedPreferences.eliminarDatos(getContext(), "cambio");
                                ClaseSharedPreferences.guardarDatos(getContext(),"cambio","no");
                                ((MainActivity) getActivity()).activarMenu();
                                lista.remove(pos);
                                proyectos.setAdapter(adaptador);
                            });
                        }else{
                            builder.setTitle("Confirmación");
                            builder.setMessage("Eliminar el contenido de la aplicación. ¿Continuar?");
                            //Si el usuario pulsa en si, procedemos a borrar el contenido.
                            builder.setPositiveButton("Sí", (dialog, which) -> {
                                borrarApp(files[finalI1]);
                                ClaseSharedPreferences.eliminarDatos(getContext(), "cambio");
                                ClaseSharedPreferences.guardarDatos(getContext(),"cambio","no");
                                ((MainActivity) getActivity()).activarMenu();
                                lista.remove(pos);
                                proyectos.setAdapter(adaptador);
                            });
                        }
                        builder.setNegativeButton("No", (dialog, which) -> {
                            proyectos.setAdapter(adaptador);
                        });
                        builder.show();
                    }
                }
                proyectos.setAdapter(adaptador);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(proyectos);
        //--------------------------------------------------------
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
    //Metodo encargado de mostrar u ocultar el menu de ordenar
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if(menuOrdenar){
            menu.findItem(R.id.ordenar).setVisible(true);
        }else {
            menu.findItem(R.id.ordenar).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    //distintas opciones del menu ordenar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.ordFechaAsc:
                adaptador.cambiarOrden("fechaAsc");
                proyectos.setAdapter(adaptador);
                break;
            case R.id.ordFechaDesc:
                adaptador.cambiarOrden("fechaDesc");
                proyectos.setAdapter(adaptador);
                break;
            case R.id.ordTituloAsc:
                adaptador.cambiarOrden("tituloAsc");
                proyectos.setAdapter(adaptador);
                break;
            case R.id.ordTituloDesc:
                adaptador.cambiarOrden("tituloDesc");
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
    //------------------------------------------------------------------------------------------------
    // Metodos encargados para borrar los archivos
    private void borrarApp(File f) {
        String path = getContext().getExternalFilesDir(null).toString();
        File carpetaFicheros = new File(path);
        File[] files = carpetaFicheros.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equalsIgnoreCase(f.getName()) || files[i].getName().equalsIgnoreCase(f.getName() + ".zip")) {
                if(files[i].isDirectory()){
                    borrarDirectorio(files[i]);
                }
                files[i].delete();
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if(!idioma.equalsIgnoreCase("es")){
            builder.setMessage("File successfully deleted.").setTitle("Deleted")
                    .setPositiveButton("Ok", (dialogInterface, which) -> {
                        dialogInterface.cancel();
                    });
        }else {
            builder.setMessage("Archivo eliminado correctamente.").setTitle("Eliminado")
                    .setPositiveButton("Ok", (dialogInterface, which) -> {
                        dialogInterface.cancel();
                    });
        }
        builder.show();
    }

    private void borrarDirectorio(File f) {
        File[] archivos = f.listFiles();
        for (int i = 0; i < archivos.length; i++) {
            if (archivos[i].isDirectory()) {
                borrarDirectorio(archivos[i]);
            } else {
                archivos[i].delete();
            }
        }
    }
    //------------------------------------------------------------------------------------------------
    // Metodo que muestra por pantalla un Dialog para seleccionar desde donde acceder al archivo
    private void modoAccederRecurso(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if(!idioma.equalsIgnoreCase("es")){
            builder.setTitle("Resource selector");
            builder.setMessage("from where you want to access the file?");
            builder.setPositiveButton("Local file", (dialog, which) -> {
                verificarPermisos();
            });
            builder.setNegativeButton("From Url", (dialog, which) -> {
                introducirURL();
            });
        }else{
            builder.setTitle("Seleccion de recurso");
            builder.setMessage("¿Desde donde desea acceder al recurso?");
            builder.setPositiveButton("Archivo Local", (dialog, which) -> {
                verificarPermisos();
            });
            builder.setNegativeButton("Desde Url", (dialog, which) -> {
                introducirURL();
            });
        }
        builder.show();
    }
    // Metodo encargado de mostrar por pantalla un Dialog para introducir la URL del archivo a descargar
    public void introducirURL(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if(!idioma.equalsIgnoreCase("es")){
            builder.setTitle("Enter URL");
            builder.setMessage("Please enter the URL of the resource you want to access");
            builder.setView(input);
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        url = input.getText().toString();
                        descargarArchivo(url);
                    }catch (Exception e){
                        Toast.makeText(getContext(),"Error entering the url",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }else{
            builder.setTitle("Introducir URL");
            builder.setMessage("Por favor introduzca la URL del recurso al que desea acceder");
            builder.setView(input);
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        url = input.getText().toString();
                        descargarArchivo(url);
                    }catch (Exception e){
                        Toast.makeText(getContext(),"Error al introducir la url",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        builder.show();
    }
    // Metodo encargado de realizar la descarga del archivo
    private void descargarArchivo(String url){
        Uri uri = Uri.parse(url);
        getActivity().registerReceiver(onDownloadComplete, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        DownloadManager.Request request = new DownloadManager.Request(uri);
        String title = URLUtil.guessFileName(url,null,null);
        request.setTitle(title);
        String cookie = CookieManager.getInstance().getCookie(url);
        if(!idioma.equalsIgnoreCase("es")){
            request.setDescription("Downloading file");
            request.addRequestHeader("cookie",cookie);
        }else{
            request.setDescription("Descargando archivo");
            request.addRequestHeader("cookie",cookie);
        }
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title);
        DownloadManager downloadManager = (DownloadManager)getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
        mFileDownloadedId = downloadManager.enqueue(request);
        if(!idioma.equalsIgnoreCase("es")){
            Toast.makeText(getContext(),"Starting download",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getContext(),"Comenzando descarga",Toast.LENGTH_SHORT).show();
        }

        this.proyectos.setVisibility(View.INVISIBLE);

        ((MainActivity) getActivity()).ocultarMenu();

    }

    // Metodo que se ejecuta una vez se haya descargado el archivo, cambia el fragment para despues abrir el archivo descargado.
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((MainActivity) getActivity()).mostrarMenu();
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == mFileDownloadedId) {
                // Archivo recibido
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = manager.getUriForDownloadedFile(mFileDownloadedId);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FileFromURL fileFromURL = FileFromURL.newInstance(uri);
                FileFromURL.hideMenu=true;
                fragmentTransaction.replace(R.id.nav_host_fragment, fileFromURL);
                fragmentTransaction.commit();
            }
        }
    };

    //--------------------------------------------------------------------------------------------------------------------------------

}