

document.addEventListener("DOMContentLoaded", function () {
    const formAtencion = document.getElementById("formAtencion");

    if (formAtencion) {
        formAtencion.addEventListener("submit", function (event) {
            event.preventDefault(); // Evita que se envíe por defecto

            // Limpiar errores anteriores
            document.querySelectorAll(".text-danger").forEach(el => el.textContent = "");

            // Capturar valores
            const nombre = document.getElementById("nombre").value.trim();
            const apellidos = document.getElementById("apellidos").value.trim();
            const telefono = document.getElementById("telefono").value.trim();
            const correo = document.getElementById("correo").value.trim();
            const distrito = document.getElementById("distrito").value;
            const horario = document.getElementById("horario").value;
            const referencia = document.getElementById("referencia").value.trim();
            const requerimiento = document.getElementById("requerimiento").value;
            const mensaje = document.getElementById("mensaje").value.trim();

            // Expresiones regulares tradicionales
            const soloLetras = /^[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+$/;
            const soloNumeros = /^\d+$/;
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

            let esValido = true;

            // Validar Nombre
            if (nombre === "") {
                document.getElementById("error-nombre").textContent = "El campo nombre es obligatorio.";
                esValido = false;
            } else if (!soloLetras.test(nombre)) {
                document.getElementById("error-nombre").textContent = "El nombre solo debe contener letras.";
                esValido = false;
            }

            // Validar Apellidos
            if (apellidos === "") {
                document.getElementById("error-apellidos").textContent = "El campo apellidos es obligatorio.";
                esValido = false;
            } else if (!soloLetras.test(apellidos)) {
                document.getElementById("error-apellidos").textContent = "Los apellidos solo deben contener letras.";
                esValido = false;
            }

            // Validar Teléfono
            if (telefono === "") {
                document.getElementById("error-telefono").textContent = "El campo teléfono es obligatorio.";
                esValido = false;
            } else if (!soloNumeros.test(telefono) || telefono.length !== 9) {
                document.getElementById("error-telefono").textContent = "Ingrese un número de teléfono válido (9 dígitos).";
                esValido = false;
            }

            // Validar Correo
            if (correo === "") {
                document.getElementById("error-correo").textContent = "El campo correo electrónico es obligatorio.";
                esValido = false;
            } else if (!emailRegex.test(correo)) {
                document.getElementById("error-correo").textContent = "Ingrese un correo electrónico válido.";
                esValido = false;
            }

            // Validar Distrito
            if (!distrito) {
                document.getElementById("error-distrito").textContent = "Debe seleccionar un distrito.";
                esValido = false;
            }

            // Validar Horario
            if (!horario) {
                document.getElementById("error-horario").textContent = "Debe seleccionar un horario de llamada.";
                esValido = false;
            }

            // Validar Referencia
            if (referencia === "") {
                document.getElementById("error-referencia").textContent = "La dirección o referencia es obligatoria.";
                esValido = false;
            }

            // Validar Requerimiento
            if (!requerimiento) {
                document.getElementById("error-requerimiento").textContent = "Debe seleccionar un tipo de requerimiento.";
                esValido = false;
            }

            // Validar Mensaje
            if (mensaje === "") {
                document.getElementById("error-mensaje").textContent = "El mensaje detallado es obligatorio.";
                esValido = false;
            }

            // Si pasa todas las validaciones
            if (esValido) {
                alert("¡Solicitud enviada con éxito! Nos pondremos en contacto contigo a la brevedad.");
                formAtencion.reset();
            }
        });
    }
});