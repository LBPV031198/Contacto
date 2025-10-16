package controlador;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import vista.ventana;
import modelo.*;

// Controla todo lo que pasa en la ventana. Implementa los 3 'listeners' que siempre usamos.
public class logica_ventana implements ActionListener, ListSelectionListener, ItemListener {
    private ventana miVentana; // Referencia a la GUI principal.
    private String nombres, email, telefono, categoriaSeleccionada = ""; // Datos del contacto.
    private persona contactoActual; // El contacto que estoy manipulando.
    private List<persona> listaDeContactos; // La lista maestra de todos los contactos.
    private boolean esFavorito = false; // Estado del checkbox.

    // Constructor, inicializa y setea todos los 'listeners'.
    public logica_ventana(ventana delegado) {
        // Asigna la ventana (la 'view').
        this.miVentana = delegado;
        // Cargamos lo que está guardado al inicio.
        cargarContactosDesdeArchivo(); 
        
        // Seteo los listeners a los botones.
        this.miVentana.btn_add.addActionListener(this);
        this.miVentana.btn_eliminar.addActionListener(this);
        this.miVentana.btn_modificar.addActionListener(this);
        
        // Listener para la lista (cuando seleccionas un contacto).
        this.miVentana.lst_contactos.addListSelectionListener(this);
        
        // Listeners para los componentes que cambian de estado (combo y check).
        this.miVentana.cmb_categoria.addItemListener(this);
        this.miVentana.chb_favorito.addItemListener(this);
    }

    // Método para leer los campos de texto y cargarlos a las variables locales.
    private void leerCamposGUI() {
        nombres = miVentana.txt_nombres.getText();
        email = miVentana.txt_email.getText();
        telefono = miVentana.txt_telefono.getText();
    }

    // Carga los contactos del archivo y los mete en el JList.
    private void cargarContactosDesdeArchivo() {
        try {
            // Leemos con el DAO. Creamos un DAO 'al vuelo' solo para leer.
            listaDeContactos = new personaDAO(new persona()).leerArchivo();
            DefaultListModel<String> modeloLista = new DefaultListModel<>();
            // Iteramos la lista y llenamos el modelo del JList.
            for (persona contacto : listaDeContactos) {
                modeloLista.addElement(contacto.formatoLista()); // Usamos el método de la clase persona.
            }
            // Seteamos el modelo a la lista visual.
            miVentana.lst_contactos.setModel(modeloLista);
        } catch (IOException e) {
            // Si hay un error de lectura, mostramos un mensaje feo.
            JOptionPane.showMessageDialog(miVentana, "Error cargando contactos... ¿El archivo existe?");
        }
    }

    // Deja todo como estaba al inicio (limpia).
    private void limpiarCampos() {
        // Borra los textos.
        miVentana.txt_nombres.setText("");
        miVentana.txt_telefono.setText("");
        miVentana.txt_email.setText("");
        
        // Resetea las variables globales
        categoriaSeleccionada = "";
        esFavorito = false;
        
        // Resetea los componentes de selección.
        miVentana.chb_favorito.setSelected(esFavorito);
        miVentana.cmb_categoria.setSelectedIndex(0); // Vuelve a la primera opción ("Elija una Categoría")
        
        leerCamposGUI(); // Actualiza las variables con los campos vacíos.
        cargarContactosDesdeArchivo(); // Recarga la lista para asegurar.
    }

    // Manejo de eventos de botones (Add, Eliminar, Modificar).
    @Override
    public void actionPerformed(ActionEvent e) {
        leerCamposGUI(); // Siempre leer antes de hacer algo.

        // Si le di al botón de agregar...
        if (e.getSource() == miVentana.btn_add) {
            // Validaciones básicas (que no estén vacíos).
            if ((!nombres.isEmpty()) && (!telefono.isEmpty()) && (!email.isEmpty())) {
                // Validar que eligió una categoría.
                if ((!categoriaSeleccionada.equals("Elija una Categoria")) && (!categoriaSeleccionada.isEmpty())) {
                    // Crea el objeto y guarda.
                    contactoActual = new persona(nombres, telefono, email, categoriaSeleccionada, esFavorito);
                    new personaDAO(contactoActual).escribirArchivo();
                    
                    limpiarCampos();
                    JOptionPane.showMessageDialog(miVentana, "¡Contacto Guardado!");
                } else {
                    // Error si no eligió categoría.
                    JOptionPane.showMessageDialog(miVentana, "¡¡¡Tienes que elegir una Categoría!!!");
                }
            } else {
                // Error si falta algún campo de texto.
                JOptionPane.showMessageDialog(miVentana, "¡Debes llenar los campos de Nombre, Teléfono y Email!");
            }
        } else if (e.getSource() == miVentana.btn_eliminar) {
            // TODO: Implementar el borrado del contacto seleccionado.
        } else if (e.getSource() == miVentana.btn_modificar) {
            // TODO: Implementar la modificación del contacto seleccionado.
        }
    }

    // Manejo de la selección en la JList.
    @Override
    public void valueChanged(ListSelectionEvent e) {
        // Asegurarse de que el evento ya terminó (para que no salte varias veces).
        if (!e.getValueIsAdjusting()) { 
            int index = miVentana.lst_contactos.getSelectedIndex();
            // Verifica que algo esté seleccionado.
            if (index != -1) {
                // La primera línea suele ser un 'header', si la lista no es vacía, cargamos.
                // ¡OJO! Si tienes un header, el índice en la GUI es +1 respecto a la listaDeContactos.
                if (index < listaDeContactos.size()) {
                    cargarContactoEnCampos(index);
                }
            }
        }
    }

    // Pone los datos del contacto seleccionado en los campos de texto.
    private void cargarContactoEnCampos(int index) {
        // Obtenemos el objeto persona de la lista maestra.
        persona contactoSeleccionado = listaDeContactos.get(index);
        
        // Llenamos los campos de la GUI.
        miVentana.txt_nombres.setText(contactoSeleccionado.getNombre());
        miVentana.txt_telefono.setText(contactoSeleccionado.getTelefono());
        miVentana.txt_email.setText(contactoSeleccionado.getEmail());
        
        // Setear el estado de favorito.
        miVentana.chb_favorito.setSelected(contactoSeleccionado.isFavorito());
        
        // Setear la categoría en el combobox.
        miVentana.cmb_categoria.setSelectedItem(contactoSeleccionado.getCategoria());
    }

    // Manejo de eventos del ComboBox y CheckBox.
    @Override
    public void itemStateChanged(ItemEvent e) {
        // Si el evento viene del combo de categoría.
        if (e.getSource() == miVentana.cmb_categoria) {
            // Solo capturamos cuando se selecciona uno nuevo, no cuando se deselecciona.
             if (e.getStateChange() == ItemEvent.SELECTED) {
                categoriaSeleccionada = miVentana.cmb_categoria.getSelectedItem().toString();
             }
        } 
        // Si el evento viene del checkbox de favorito.
        else if (e.getSource() == miVentana.chb_favorito) {
            esFavorito = miVentana.chb_favorito.isSelected();
        }
    }
}