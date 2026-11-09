package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class Planta {
	protected String nombre;
	private String direccion;
	private double capacidad;
	private boolean estado;
	private String fechaInicio;
	private HashMap<LocalDateTime, RegistroMonitoreo> monitoreos;
	private LinkedHashMap<LocalDateTime, Boolean> actividad;
	private LocalDateTime ultimoCambio;

	public Planta(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio) {
		this.nombre = nombre;
		this.direccion = direccion;
		this.capacidad = capacidad;
		this.estado = estado;
		this.fechaInicio = fechaInicio;
		this.monitoreos = new LinkedHashMap<LocalDateTime, RegistroMonitoreo>();
		this.actividad = new LinkedHashMap<LocalDateTime, Boolean>();
		this.ultimoCambio = LocalDateTime.parse(fechaInicio);
		this.actividad.put(this.ultimoCambio, this.estado);
	}

	public abstract boolean cumpleCuota(String fechaHora);

	public abstract double capacidadMinimaCuota();

	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraOriginal = LocalDateTime.parse(fechaHora);
		LocalDateTime fechaHoraTruncada = fechaHoraOriginal.truncatedTo(ChronoUnit.HOURS);
		validarRegistroMonitoreo(fechaHoraOriginal, fechaHoraTruncada);
		RegistroMonitoreo registro = new RegistroMonitoreo(this.nombre, fechaHoraTruncada, incidencias,
				energiaGenerada);
		guardarMonitoreo(fechaHoraOriginal, fechaHoraTruncada, registro);

	}
	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias, double velocidadViento) {
        throw new RuntimeException("Este tipo de planta no admite monitoreo eolico.");
    }

    public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias, double co2Emitido, double combustibleUtilizado) {
        throw new RuntimeException("Este tipo de planta no admite monitoreo termico.");
    }
    
	protected void validarRegistroMonitoreo(LocalDateTime fechaHoraParseada, LocalDateTime fechaHoraTruncada) {
		LocalDateTime fechaHoraInicio = LocalDateTime.parse(fechaInicio);
	    if (this.monitoreos.containsKey(fechaHoraTruncada)) {
	        throw new RuntimeException("Ya hay un monitoreo para esta fecha + hora.");
	    }
	    if (fechaHoraParseada.isBefore(fechaHoraInicio)) {
	        throw new RuntimeException("No se puede hacer un registro anterior a la puesta en marcha.");
	    }
	    if (!estaActivaEn(fechaHoraParseada.toString())) {
	        throw new RuntimeException("La planta no esta activada en esta fecha + hora.");
	    }
		
		
	}

	public String reportePocoViento() {
		return "";
	}
	
	public void activar(String fecha) {
		if (!SmartGrids.esFechaHoraValida(fecha)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fecha);
		if (this.estado) {
			throw new RuntimeException("La planta ya esta activa");
		}
		if(fechaHoraParseada.isBefore(ultimoCambio)) {
			throw new RuntimeException("La actividad no puede ser modificada para dias y horas menores al ultimo registro");
		}
		this.estado = true;
		actividad.put(fechaHoraParseada, true);
		ultimoCambio = fechaHoraParseada;

	}
	public void desactivar(String fecha) {
		if (!SmartGrids.esFechaHoraValida(fecha)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fecha);
		if (!this.estado) {
			throw new RuntimeException("La planta ya esta inactiva");
		}
		if(fechaHoraParseada.isBefore(ultimoCambio)) {
			throw new RuntimeException("La actividad no puede ser modificada para dias y horas menores al ultimo registro");
		}
		this.estado = false;
		actividad.put(fechaHoraParseada, false);
		ultimoCambio = fechaHoraParseada;
	}

	public double totalGenerado() {
		double total = 0;

		for (RegistroMonitoreo monitoreo : monitoreos.values()) {
			total += monitoreo.getEnergiaGenerada();
		}
		return total;
	}

	public int[] incidenciasPorHora() {
		int[] resultado = new int[24];


		for (Map.Entry<LocalDateTime, RegistroMonitoreo> entry : monitoreos.entrySet()) {
			int hora = entry.getKey().getHour();
			int incidencias = entry.getValue().getIncidencias();
			resultado[hora] += incidencias;
		}
		return resultado;
	}

	public int[] histogramaCumplimiento(String fechaDesde, String fechaHasta) {
		LocalDate desde = LocalDate.parse(fechaDesde);
		LocalDate hasta = LocalDate.parse(fechaHasta);
		int[] resultado = new int[24];

		for (Map.Entry<LocalDateTime, RegistroMonitoreo> entry : monitoreos.entrySet()) {
			LocalDateTime tiempoRegistro = entry.getKey();
			if (!tiempoRegistro.toLocalDate().isBefore(desde) && !tiempoRegistro.toLocalDate().isAfter(hasta)) {
				if (cumpleCuota(tiempoRegistro.toString())) {
					resultado[tiempoRegistro.getHour()] += 1;
				}
			}
		}
		return resultado;
	}
	public boolean nuncaCumplioCuota() {
        for (LocalDateTime fechaHora : monitoreos.keySet()) {
            if (cumpleCuota(fechaHora.toString())) {
                return false;
            }
        }
        return true;
    }

	public int limpiarMonitoreosSinDatos() {
        int borrados = 0;
        Iterator<RegistroMonitoreo> it = monitoreos.values().iterator();
        while (it.hasNext()) {
            RegistroMonitoreo r = it.next();
            if (r.getEnergiaGenerada() == 0 && r.getIncidencias() == 0) {
                it.remove();
                borrados++;
            }
        }
        return borrados;
    }
	
	public String getNombre() {
		return nombre;
	}

	public String getDireccion() {
		return direccion;
	}

	public double getCapacidad() {
		return capacidad;
	}

	public boolean isEstado() {
		return estado;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getDetalle());
		if (!this.monitoreos.isEmpty()) {
			for (RegistroMonitoreo monitoreo : this.monitoreos.values()) {
				sb.append("\n	• ").append(monitoreo.toString());
			}
		}
		return sb.toString();
	}

	public String getDetalle() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.nombre).append(" ");
		sb.append(this.estado ? "(ACTIVA): " : "(INACTIVA): ");
		sb.append(this.fechaInicio);

		String sp = sb.toString();
		return sp;
	}
	public void registrarFuenteSiEsRenovable(Map<String, Integer> fuentePorPlanta) {
		return;
    }
	public boolean estaActivaEn(String fechaHora) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
	
		LocalDateTime fecha = LocalDateTime.parse(fechaHora);
        boolean activaEnFecha = false;
        for (Map.Entry<LocalDateTime, Boolean> entry : actividad.entrySet()) {
            if (!entry.getKey().isAfter(fecha)) {
                activaEnFecha = entry.getValue();
            } else {
                break;
            }
        }
        return activaEnFecha;
	}

	protected void guardarMonitoreo(LocalDateTime fechaHoraOriginal, LocalDateTime fechaHoraTruncada, RegistroMonitoreo registro) {
		monitoreos.put(fechaHoraTruncada, registro);
		
		if (fechaHoraOriginal.isAfter(ultimoCambio)) {
			ultimoCambio = fechaHoraOriginal;
		}
	}
	protected RegistroMonitoreo obtenerMonitoreo(LocalDateTime fechaTruncada) {
		return this.monitoreos.get(fechaTruncada);
	}
	
	protected Collection<RegistroMonitoreo> obtenerMonitoreos() {
		return Collections.unmodifiableCollection(monitoreos.values());
	}
	

}
