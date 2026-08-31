console.log("ALIVIO cargado correctamente");


// ==========================================
// ALIVIO - FORMULARIO DE SOLICITUD
// ==========================================


// ------------------------------------------
// 1. OBTENER ELEMENTOS DEL HTML
// ------------------------------------------

const formulario = document.getElementById("formSolicitud");

const nombre = document.getElementById("nombre");

const telefono = document.getElementById("telefono");

const servicio = document.getElementById("servicio");

const distrito = document.getElementById("distrito");


// ------------------------------------------
// 2. ESCUCHAR CUANDO EL USUARIO ENVÍA
//    EL FORMULARIO
// ------------------------------------------

formulario.addEventListener("submit", function(event) {

    // Evitamos que la página se recargue
    event.preventDefault();


    // --------------------------------------
    // 3. VARIABLES PARA SABER SI TODO
    //    ESTÁ CORRECTO
    // --------------------------------------

    let formularioValido = true;


    // --------------------------------------
    // 4. VALIDAR NOMBRE
    // --------------------------------------

    if (nombre.value.trim() === "") {

        nombre.classList.add("is-invalid");
        nombre.classList.remove("is-valid");

        formularioValido = false;

    } else {

        nombre.classList.remove("is-invalid");
        nombre.classList.add("is-valid");

    }


    // --------------------------------------
    // 5. VALIDAR TELÉFONO
    // --------------------------------------

    const numeroTelefono = telefono.value.trim();


    if (
        numeroTelefono === "" ||
        numeroTelefono.length !== 9 ||
        !/^\d+$/.test(numeroTelefono)
    ) {

        telefono.classList.add("is-invalid");
        telefono.classList.remove("is-valid");

        formularioValido = false;

    } else {

        telefono.classList.remove("is-invalid");
        telefono.classList.add("is-valid");

    }


    // --------------------------------------
    // 6. VALIDAR SERVICIO
    // --------------------------------------

    if (servicio.value === "") {

        servicio.classList.add("is-invalid");
        servicio.classList.remove("is-valid");

        formularioValido = false;

    } else {

        servicio.classList.remove("is-invalid");
        servicio.classList.add("is-valid");

    }


    // --------------------------------------
    // 7. VALIDAR DISTRITO
    // --------------------------------------

    if (distrito.value === "") {

        distrito.classList.add("is-invalid");
        distrito.classList.remove("is-valid");

        formularioValido = false;

    } else {

        distrito.classList.remove("is-invalid");
        distrito.classList.add("is-valid");

    }


    // --------------------------------------
    // 8. COMPROBAR RESULTADO FINAL
    // --------------------------------------

    if (formularioValido) {

        console.log("Formulario válido");

        console.log("Nombre:", nombre.value);

        console.log("Teléfono:", telefono.value);

        console.log("Servicio:", servicio.value);

        console.log("Distrito:", distrito.value);

        alert("¡Solicitud registrada correctamente!");

    }

});