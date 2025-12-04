package com.nilson.appsportmate.features.user.ui.eventosPrivados.crearEventoPrivate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.nilson.appsportmate.R;
import com.nilson.appsportmate.common.utils.Preferencias;

import java.util.Calendar;

public class CrearEventoUserPrivateFragment extends Fragment {

    // UI
    private EditText etNombreDeporte, etCantidadJugadores, etFecha, etHora,
            etDescripcionEvento, etReglasEvento, etMateriales, etUrlPueblo;

    private MaterialButton btnCrearEvento, btnGestionEventos;
    private Button btnLogout;

    // VM
    private CrearEventoUserPrivateViewModel vm;

    // Estado
    private String uidUsuario;

    public CrearEventoUserPrivateFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // NECESARIO PARA RECIBIR LAS OPCIONES DE MENÚ
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_evento_user_private, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(CrearEventoUserPrivateViewModel.class);

        uidUsuario = Preferencias.obtenerUid(requireContext());
        if (TextUtils.isEmpty(uidUsuario)) {
            Toast.makeText(requireContext(), "Error: UID no encontrado.", Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).navigate(R.id.loginFragment);
            return;
        }

        vm.setUidUsuario(uidUsuario);

        bindViews(view);
        setupClicks();
        observeVm();
    }



    // ---------------------------
    // Bind Views
    // ---------------------------

    private void bindViews(View v) {
        etNombreDeporte      = v.findViewById(R.id.etNombreDeporte);
        etCantidadJugadores  = v.findViewById(R.id.etCantidadJugadores);
        etFecha              = v.findViewById(R.id.etFecha);
        etHora               = v.findViewById(R.id.etHora);
        etDescripcionEvento  = v.findViewById(R.id.etDescripcionEvento);
        etReglasEvento       = v.findViewById(R.id.etReglasEvento);
        etMateriales         = v.findViewById(R.id.etMateriales);
        etUrlPueblo          = v.findViewById(R.id.etUrlPueblo);

        btnCrearEvento       = v.findViewById(R.id.btnCrearEvento);
        btnGestionEventos    = v.findViewById(R.id.btnGestionEventos);
        btnLogout            = v.findViewById(R.id.btnLogout);
    }

    // ---------------------------
    // Click Listeners
    // ---------------------------

    private void setupClicks() {

        etFecha.setOnClickListener(v -> mostrarDatePicker());
        etHora.setOnClickListener(v -> mostrarTimePicker());

        btnCrearEvento.setOnClickListener(v -> vm.crearEventoParticular(
                txt(etNombreDeporte),
                txtInt(etCantidadJugadores),
                txt(etFecha),
                txt(etHora),
                txt(etDescripcionEvento),
                txt(etReglasEvento),
                txt(etMateriales),
                txt(etUrlPueblo)
        ));

        btnGestionEventos.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.action_crearEventoUserPrivateFragment_to_gestionEventosUserPrivateFragment);
        });



        btnLogout.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(v);
            boolean popped = nav.popBackStack(R.id.inicioFragment, false);
            if (!popped) {
                nav.navigate(R.id.action_global_inicioFragment);
            }
        });
    }

    // ---------------------------
    // Observers
    // ---------------------------

    private void observeVm() {

        vm.getToast().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                vm.consumeToast();
            }
        });

        vm.getClearForm().observe(getViewLifecycleOwner(), clear -> {
            if (clear != null && clear) {
                limpiarFormulario();
                vm.onFormCleared();
            }
        });

        vm.getNavigateToGestionEventos().observe(getViewLifecycleOwner(), go -> {
            if (go != null && go) {
                NavController nav = Navigation.findNavController(requireView());
                nav.navigate(R.id.action_global_gestionEventosUserPrivateFragment);
                vm.onNavigatedToGestionEventos();
            }
        });

    }

    // ---------------------------
    // Pickers
    // ---------------------------

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) ->
                        etFecha.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void mostrarTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) ->
                        etHora.setText(String.format("%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    // ---------------------------
    // Utils
    // ---------------------------

    private static String txt(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static Integer txtInt(EditText et) {
        try {
            String t = txt(et);
            return t.isEmpty() ? null : Integer.parseInt(t);
        } catch (Exception e) {
            et.setError("Número inválido");
            return null;
        }
    }

    private void limpiarFormulario() {
        etNombreDeporte.setText("");
        etCantidadJugadores.setText("");
        etFecha.setText("");
        etHora.setText("");
        etDescripcionEvento.setText("");
        etReglasEvento.setText("");
        etMateriales.setText("");
        etUrlPueblo.setText("");

        etNombreDeporte.setError(null);
        etCantidadJugadores.setError(null);
        etFecha.setError(null);
        etHora.setError(null);
        etDescripcionEvento.setError(null);
        etReglasEvento.setError(null);
        etMateriales.setError(null);
        etUrlPueblo.setError(null);
    }
}
