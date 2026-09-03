
let solicitudes = [];


//  ELEMENTOS DEL FORMULARIO

const formulario = document.getElementById("formSolicitud");
const nombre = document.getElementById("nombre");
const telefono = document.getElementById("telefono");
const servicio = document.getElementById("servicio");
const distrito = document.getElementById("distrito");
const fecha = document.getElementById("fecha");
const mensaje = document.getElementById("mensaje");
const modalSolicitudEl = document.getElementById("modalSolicitud");
const modalExitoEl = document.getElementById("modalExito");


// CAMPOS DINÁMICOS DEL FORMULARIO

const camposAdultoMayor = document.getElementById("camposAdultoMayor");
const camposEnfermeria = document.getElementById("camposEnfermeria");
const camposCuracion = document.getElementById("camposCuracion");
const camposAcompanamiento = document.getElementById("camposAcompanamiento");
const camposEquipamiento = document.getElementById("camposEquipamiento");
const camposOrientacion = document.getElementById("camposOrientacion");

const edadPaciente = document.getElementById("edadPaciente");
const tipoCuidado = document.getElementById("tipoCuidado");
const duracion = document.getElementById("duracion");
const tipoAtencion = document.getElementById("tipoAtencion");
const tipoCuracion = document.getElementById("tipoCuracion");
const tipoAcompanamiento = document.getElementById("tipoAcompanamiento");
const producto = document.getElementById("producto");


//  ELEMENTOS DE LA TABLA

const tablaSolicitudes = document.getElementById("tablaSolicitudes");
const contadorSolicitudes = document.getElementById("contadorSolicitudes");
const sinSolicitudes = document.getElementById("sinSolicitudes");
const buscarSolicitud = document.getElementById("buscarSolicitud");
const filtroEstado = document.getElementById("filtroEstado");
const detalleSolicitudCuerpo = document.getElementById("detalleSolicitudCuerpo");
const modalDetalleEl = document.getElementById("modalDetalle");


//  CARGAR SOLICITUDES GUARDADAS
const solicitudesGuardadas = localStorage.getItem("solicitudesALIVIO");

if (solicitudesGuardadas) {
    solicitudes = JSON.parse(solicitudesGuardadas);
}


//  GUARDAR EN LOCALSTORAGE

function guardarEnLocalStorage() {
    localStorage.setItem("solicitudesALIVIO", JSON.stringify(solicitudes));
}

function escapeHtml(texto) {
    const div = document.createElement("div");
    div.textContent = texto == null ? "" : String(texto);
    return div.innerHTML;
}

function textoOpcion(selectEl) {
    if (!selectEl || !selectEl.value) {
        return "";
    }

    return selectEl.options[selectEl.selectedIndex].text;
}


// OCULTAR CAMPOS DINÁMICOS

function ocultarCampos() {
    [
        camposAdultoMayor,
        camposEnfermeria,
        camposCuracion,
        camposAcompanamiento,
        camposEquipamiento,
        camposOrientacion
    ].forEach(function (bloque) {
        if (bloque) {
            bloque.classList.add("d-none");
        }
    });
}

// 8. MOSTRAR CAMPOS SEGÚN EL SERVICIO

if (servicio) {
    servicio.addEventListener("change", function () {
        ocultarCampos();

        switch (servicio.value) {
            case "adulto-mayor":
                camposAdultoMayor.classList.remove("d-none");
                break;
            case "enfermeria":
                camposEnfermeria.classList.remove("d-none");
                break;
            case "curaciones":
                camposCuracion.classList.remove("d-none");
                break;
            case "acompanamiento":
                camposAcompanamiento.classList.remove("d-none");
                break;
            case "equipamiento":
                camposEquipamiento.classList.remove("d-none");
                break;
            case "orientacion":
                camposOrientacion.classList.remove("d-none");
                break;
        }
    });
}


// 9-13. VALIDACIONES

function validarNombre() {
    if (!nombre.value.trim()) {
        nombre.classList.add("is-invalid");
        nombre.classList.remove("is-valid");
        return false;
    }

    nombre.classList.remove("is-invalid");
    nombre.classList.add("is-valid");
    return true;
}

function validarTelefono() {
    const numero = telefono.value.trim();
    const telefonoValido = /^[0-9]{9}$/.test(numero);

    if (!telefonoValido) {
        telefono.classList.add("is-invalid");
        telefono.classList.remove("is-valid");
        return false;
    }

    telefono.classList.remove("is-invalid");
    telefono.classList.add("is-valid");
    return true;
}

function validarServicio() {
    if (servicio.value === "") {
        servicio.classList.add("is-invalid");
        servicio.classList.remove("is-valid");
        return false;
    }

    servicio.classList.remove("is-invalid");
    servicio.classList.add("is-valid");
    return true;
}

function validarDistrito() {
    if (distrito.value === "") {
        distrito.classList.add("is-invalid");
        distrito.classList.remove("is-valid");
        return false;
    }

    distrito.classList.remove("is-invalid");
    distrito.classList.add("is-valid");
    return true;
}

function validarFecha() {
    if (fecha.value === "") {
        fecha.classList.add("is-invalid");
        fecha.classList.remove("is-valid");
        return false;
    }

    fecha.classList.remove("is-invalid");
    fecha.classList.add("is-valid");
    return true;
}

// 14. GENERAR CÓDIGO DE SOLICITUD

function generarCodigoSolicitud() {
    let numero = solicitudes.length + 1;
    let codigo = "SOL-" + String(numero).padStart(3, "0");

    while (
        solicitudes.some(function (solicitud) {
            return solicitud.codigo === codigo;
        })
    ) {
        numero++;
        codigo = "SOL-" + String(numero).padStart(3, "0");
    }

    return codigo;
}


// 15. NOMBRE DEL SERVICIO Y DETALLE

function obtenerNombreServicio() {
    return textoOpcion(servicio);
}

function obtenerDetalleServicio() {
    switch (servicio.value) {
        case "adulto-mayor":
            return [
                edadPaciente && edadPaciente.value
                    ? "Edad: " + edadPaciente.value
                    : "",
                textoOpcion(tipoCuidado)
                    ? "Apoyo: " + textoOpcion(tipoCuidado)
                    : "",
                textoOpcion(duracion)
                    ? "Duración: " + textoOpcion(duracion)
                    : ""
            ].filter(Boolean).join(" · ");

        case "enfermeria":
            return textoOpcion(tipoAtencion);

        case "curaciones":
            return tipoCuracion ? tipoCuracion.value.trim() : "";

        case "acompanamiento":
            return textoOpcion(tipoAcompanamiento);

        case "equipamiento":
            return textoOpcion(producto);

        default:
            return mensaje && mensaje.value.trim()
                ? mensaje.value.trim()
                : "Orientación general";
    }
}


// 16. CREAR Y GUARDAR SOLICITUD

function guardarSolicitud() {
    const solicitud = {
        codigo: generarCodigoSolicitud(),
        cliente: nombre.value.trim(),
        telefono: telefono.value.trim(),
        servicio: obtenerNombreServicio(),
        detalle: obtenerDetalleServicio(),
        distrito: distrito.value,
        fecha: fecha.value,
        estado: "Pendiente",
        mensaje: mensaje ? mensaje.value.trim() : ""
    };

    solicitudes.push(solicitud);
    guardarEnLocalStorage();
    return solicitud;
}

// 17. CLASE DEL ESTADO

function obtenerClaseEstado(estado) {
    switch (estado) {
        case "Pendiente":
            return "estado-pendiente";
        case "Confirmada":
            return "estado-confirmada";
        case "Finalizada":
            return "estado-finalizada";
        case "Cancelada":
            return "estado-cancelada";
        default:
            return "";
    }
}

// 18. MOSTRAR SOLICITUDES EN LA TABLA

function mostrarSolicitudes(lista) {
    if (!tablaSolicitudes) {
        return;
    }

    const datos = lista || solicitudes;

    tablaSolicitudes.innerHTML = "";

    if (contadorSolicitudes) {
        contadorSolicitudes.textContent = datos.length;
    }

    if (datos.length === 0) {
        if (sinSolicitudes) {
            sinSolicitudes.classList.remove("d-none");
        }
        return;
    }

    if (sinSolicitudes) {
        sinSolicitudes.classList.add("d-none");
    }

    datos.forEach(function (solicitud) {
        const fila = document.createElement("tr");

        fila.innerHTML = `
            <td><strong>${escapeHtml(solicitud.codigo)}</strong></td>
            <td>${escapeHtml(solicitud.cliente)}</td>
            <td>${escapeHtml(solicitud.servicio)}</td>
            <td>${escapeHtml(solicitud.distrito)}</td>
            <td>${escapeHtml(solicitud.fecha)}</td>
            <td>
                <span class="estado-badge ${obtenerClaseEstado(solicitud.estado)}">
                    ${escapeHtml(solicitud.estado)}
                </span>
            </td>
            <td>
                <div class="btn-group">
                    <button
                        type="button"
                        class="btn btn-sm btn-outline-primary"
                        onclick="verSolicitud('${escapeHtml(solicitud.codigo)}')"
                        title="Ver detalle">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button
                        type="button"
                        class="btn btn-sm btn-outline-secondary"
                        onclick="cambiarEstado('${escapeHtml(solicitud.codigo)}')"
                        title="Cambiar estado">
                        <i class="bi bi-arrow-repeat"></i>
                    </button>
                    <button
                        type="button"
                        class="btn btn-sm btn-outline-danger"
                        onclick="eliminarSolicitud('${escapeHtml(solicitud.codigo)}')"
                        title="Eliminar">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;

        tablaSolicitudes.appendChild(fila);
    });
}

function verSolicitud(codigo) {
    const solicitud = solicitudes.find(function (item) {
        return item.codigo === codigo;
    });

    if (!solicitud || !detalleSolicitudCuerpo || !modalDetalleEl) {
        return;
    }

    detalleSolicitudCuerpo.innerHTML = `
        <p class="mb-2"><strong>Código:</strong> ${escapeHtml(solicitud.codigo)}</p>
        <p class="mb-2"><strong>Cliente:</strong> ${escapeHtml(solicitud.cliente)}</p>
        <p class="mb-2"><strong>Teléfono:</strong> ${escapeHtml(solicitud.telefono)}</p>
        <p class="mb-2"><strong>Servicio:</strong> ${escapeHtml(solicitud.servicio)}</p>
        <p class="mb-2"><strong>Detalle:</strong> ${escapeHtml(solicitud.detalle || "—")}</p>
        <p class="mb-2"><strong>Distrito:</strong> ${escapeHtml(solicitud.distrito)}</p>
        <p class="mb-2"><strong>Fecha:</strong> ${escapeHtml(solicitud.fecha)}</p>
        <p class="mb-2"><strong>Estado:</strong> ${escapeHtml(solicitud.estado)}</p>
        <p class="mb-0"><strong>Mensaje:</strong> ${escapeHtml(solicitud.mensaje || "—")}</p>
    `;

    bootstrap.Modal.getOrCreateInstance(modalDetalleEl).show();
}


// CAMBIAR ESTADO

function cambiarEstado(codigo) {
    const solicitud = solicitudes.find(function (item) {
        return item.codigo === codigo;
    });

    if (!solicitud) {
        return;
    }

    switch (solicitud.estado) {
        case "Pendiente":
            solicitud.estado = "Confirmada";
            break;
        case "Confirmada":
            solicitud.estado = "Finalizada";
            break;
        case "Finalizada":
            solicitud.estado = "Cancelada";
            break;
        case "Cancelada":
            solicitud.estado = "Pendiente";
            break;
    }

    guardarEnLocalStorage();
    aplicarFiltros();
}


//  ELIMINAR SOLICITUD

function eliminarSolicitud(codigo) {
    const confirmar = confirm("¿Deseas eliminar esta solicitud?");

    if (!confirmar) {
        return;
    }

    solicitudes = solicitudes.filter(function (solicitud) {
        return solicitud.codigo !== codigo;
    });

    guardarEnLocalStorage();
    aplicarFiltros();
}


// BUSCAR Y FILTRAR

function aplicarFiltros() {
    if (!tablaSolicitudes) {
        return;
    }

    const texto = buscarSolicitud
        ? buscarSolicitud.value.toLowerCase().trim()
        : "";
    const estado = filtroEstado ? filtroEstado.value : "todos";

    const resultados = solicitudes.filter(function (solicitud) {
        const coincideTexto =
            solicitud.codigo.toLowerCase().includes(texto) ||
            solicitud.cliente.toLowerCase().includes(texto) ||
            solicitud.distrito.toLowerCase().includes(texto) ||
            solicitud.servicio.toLowerCase().includes(texto);

        const coincideEstado =
            estado === "todos" || solicitud.estado === estado;

        return coincideTexto && coincideEstado;
    });

    mostrarSolicitudes(resultados);
}


// EVENTOS DE FILTRO

if (buscarSolicitud) {
    buscarSolicitud.addEventListener("input", aplicarFiltros);
}

if (filtroEstado) {
    filtroEstado.addEventListener("change", aplicarFiltros);
}


// 24. ENVÍO DEL FORMULARIO

function mostrarModalExito(solicitud) {
    const codigoEl = document.getElementById("exitoCodigo");
    const clienteEl = document.getElementById("exitoCliente");
    const servicioEl = document.getElementById("exitoServicio");
    const distritoEl = document.getElementById("exitoDistrito");

    if (codigoEl) {
        codigoEl.textContent = solicitud.codigo;
    }
    if (clienteEl) {
        clienteEl.textContent = solicitud.cliente;
    }
    if (servicioEl) {
        servicioEl.textContent = solicitud.servicio;
    }
    if (distritoEl) {
        distritoEl.textContent = solicitud.distrito;
    }

    if (modalSolicitudEl) {
        const modalSolicitud = bootstrap.Modal.getInstance(modalSolicitudEl);
        if (modalSolicitud) {
            modalSolicitud.hide();
        }
    }

    if (modalExitoEl) {
        bootstrap.Modal.getOrCreateInstance(modalExitoEl).show();
    }
}

function limpiarFormulario() {
    formulario.reset();

    formulario.querySelectorAll(".is-valid, .is-invalid").forEach(function (campo) {
        campo.classList.remove("is-valid", "is-invalid");
    });

    ocultarCampos();
}

if (formulario) {
    formulario.addEventListener("submit", function (event) {
        event.preventDefault();

        const nombreValido = validarNombre();
        const telefonoValido = validarTelefono();
        const servicioValido = validarServicio();
        const distritoValido = validarDistrito();
        const fechaValida = validarFecha();

        if (
            !nombreValido ||
            !telefonoValido ||
            !servicioValido ||
            !distritoValido ||
            !fechaValida
        ) {
            return;
        }

        const nuevaSolicitud = guardarSolicitud();
        mostrarModalExito(nuevaSolicitud);
        limpiarFormulario();
    });
}

if (tablaSolicitudes) {
    aplicarFiltros();
}
