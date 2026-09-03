document.addEventListener("DOMContentLoaded", function () {

  
    document.querySelectorAll(".card").forEach(card => {
        const boton = card.querySelector(".btn");
        const tituloProducto = card.querySelector(".card-title").textContent;

        if (boton) {
            // el botón dice "COMPRAR"
            if (boton.textContent.trim() === "COMPRAR") {
                boton.setAttribute("data-bs-toggle", "modal");
                boton.setAttribute("data-bs-target", "#modalComprar");
                
                boton.addEventListener("click", function () {
                    document.getElementById("nombreProductoCompra").value = tituloProducto;
                });
            } 
            // el texto del botón dice "ALQUILAR" o "ALQUILER"
            else if (boton.textContent.includes("ALQUIL")) {
                boton.setAttribute("data-bs-toggle", "modal");
                boton.setAttribute("data-bs-target", "#modalAlquiler");
                
                boton.addEventListener("click", function () {
                    document.getElementById("nombreProductoAlquiler").value = tituloProducto;
                });
            }
        }
    });


    
    // VALIDACIÓN DEL MODAL DE COMPRA
   
    const formComprar = document.getElementById("formComprar");
    if (formComprar) {
        formComprar.addEventListener("submit", function (event) {
            event.preventDefault();

            // Limpiar errores previos de compra
            document.querySelectorAll("#modalComprar .text-danger").forEach(el => el.textContent = "");

            const cantidad = document.getElementById("cantidadCompra").value;
            const direccion = document.getElementById("direccionCompra").value.trim();
            const pago = document.getElementById("pagoCompra").value;
            let esValido = true;

            if (cantidad < 1 || cantidad === "") {
                document.getElementById("error-cantidadCompra").textContent = "Ingrese una cantidad válida (mínimo 1).";
                esValido = false;
            }

            if (direccion === "") {
                document.getElementById("error-direccionCompra").textContent = "La dirección de entrega es obligatoria.";
                esValido = false;
            }

            if (!pago) {
                document.getElementById("error-pagoCompra").textContent = "Debe seleccionar un método de pago.";
                esValido = false;
            }

            if (esValido) {
                alert("¡Pedido de compra registrado con éxito! Nos comunicaremos contigo a la brevedad.");
                formComprar.reset();
                
            
                const modalElement = document.getElementById("modalComprar");
                const modalInstance = bootstrap.Modal.getInstance(modalElement);
                if (modalInstance) modalInstance.hide();
            }
        });
    }


   
    // VALIDACIÓN DEL MODAL DE ALQUILER
  
    const formAlquiler = document.getElementById("formAlquiler");
    if (formAlquiler) {
        formAlquiler.addEventListener("submit", function (event) {
            event.preventDefault();

            // Limpiar errores previos de alquiler
            document.querySelectorAll("#modalAlquiler .text-danger").forEach(el => el.textContent = "");

            const tiempo = document.getElementById("tiempoAlquiler").value;
            const fecha = document.getElementById("fechaInicio").value;
            const direccion = document.getElementById("direccionAlquiler").value.trim();
            let esValido = true;

            if (!tiempo) {
                document.getElementById("error-tiempoAlquiler").textContent = "Seleccione el plazo estimado de alquiler.";
                esValido = false;
            }

            if (!fecha) {
                document.getElementById("error-fechaInicio").textContent = "Seleccione una fecha de inicio válida.";
                esValido = false;
            }

            if (direccion === "") {
                document.getElementById("error-direccionAlquiler").textContent = "La dirección del domicilio es obligatoria.";
                esValido = false;
            }

            if (esValido) {
                alert("¡Solicitud de alquiler registrada con éxito! Un asesor se pondrá en contacto contigo.");
                formAlquiler.reset();
                
            
                const modalElement = document.getElementById("modalAlquiler");
                const modalInstance = bootstrap.Modal.getInstance(modalElement);
                if (modalInstance) modalInstance.hide();
            }
        });
    }

});