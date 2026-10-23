package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class Planta {
	protected String nombre;
	private String direccion;
	private double capacidad;
	private boolean estado;
	private String fechaInicio;
	protected HashMap<LocalDateTime, RegistroMonitoreo> monitoreos;
	protected TreeMap<LocalDateTime, Boolean> actividad;
	private String ultimoCambio;

	public Planta(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio) {
		this.nombre = nombre;
		this.direccion = direccion;
		this.capacidad = capacidad;
		this.estado = estado;
		this.fechaInicio = fechaInicio;
		this.monitoreos = new LinkedHashMap<LocalDateTime, RegistroMonitoreo>();
		this.actividad = new TreeMap<LocalDateTime, Boolean>();
		this.ultimoCambio = fechaInicio;
	}

	public abstract boolean cumpleCuota(String fechaHora);

	public abstract double capacidadMinimaCuota();

	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fechaHora).truncatedTo(ChronoUnit.HOURS);
		validarRegistroMonitoreo(fechaHoraParseada);
		RegistroMonitoreo registro = new RegistroMonitoreo(this.nombre, fechaHoraParseada, incidencias,
				energiaGenerada);
		monitoreos.put(fechaHoraParseada, registro);
		
		

	}
	protected void validarRegistroMonitoreo(LocalDateTime fechaHoraParseada) {
		LocalDateTime fechaHoraInicio = LocalDateTime.parse(fechaInicio).truncatedTo(ChronoUnit.HOURS);
		if (this.monitoreos.containsKey(fechaHoraParseada)) {
			throw new RuntimeException("Ya hay un monitoreo para esta fecha + hora.");
		}
		if (fechaHoraParseada.isBefore(fechaHoraInicio)) {
			throw new RuntimeException("No se puede hacer un registro anterior al ultimo realizado.");
		}
		if (!estaActivaEn(fechaHoraParseada.toString())) {
			throw new RuntimeException("La planta no esta activada en esta fecha + hora.");
		}
		if (fechaHoraParseada.isAfter(LocalDateTime.parse(ultimoCambio))) {
			ultimoCambio = fechaHoraParseada.toString();
		}
		
	}

	public void activar(String fecha) {
		if (!SmartGrids.esFechaHoraValida(fecha)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fecha).truncatedTo(ChronoUnit.HOURS);
		if (this.estado) {
			throw new RuntimeException("La planta ya esta activa");
		}
		if(fechaHoraParseada.isBefore(LocalDateTime.parse(ultimoCambio))) {
			throw new RuntimeException("La actividad no puede ser modificada para dias y horas menores al ultimo registro");
		}
		this.estado = true;
		actividad.put(fechaHoraParseada, true);

	}
	public void desactivar(String fecha) {
		if (!SmartGrids.esFechaHoraValida(fecha)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fecha).truncatedTo(ChronoUnit.HOURS);
		if (!this.estado) {
			throw new RuntimeException("La planta ya esta inactiva");
		}
		if(fechaHoraParseada.isBefore(LocalDateTime.parse(ultimoCambio))) {
			throw new RuntimeException("La actividad no puede ser modificada para dias y horas menores al ultimo registro");
		}
		this.estado = false;
		actividad.put(fechaHoraParseada, false);
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

	public int[] histogramaCumplimiento(String nombre, String fechaDesde, String fechaHasta) {
		LocalDate desde = LocalDate.parse(fechaDesde);
		LocalDate hasta = LocalDate.parse(fechaHasta);
		double capacidadMinimaCuota = capacidadMinimaCuota();
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
				sb.append("\n	• " + monitoreo.toString());
			}
		}
		return sb.toString();
	}

	public HashMap<LocalDateTime, RegistroMonitoreo> getMonitoreos() {
		return monitoreos;
	}

	public String getDetalle() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.nombre + " ");
		sb.append(this.estado ? "(ACTIVA): " : "(INACTIVA): ");
		sb.append(this.fechaInicio);

		String sp = sb.toString();
		return sp;
	}
	
	public boolean estaActivaEn(String fechaHora) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		// Lo que hace floorEntry es buscar la ultima entrada de actividad anterior o igual a la fecha que se le está pasando.
		LocalDateTime fecha = LocalDateTime.parse(fechaHora).truncatedTo(ChronoUnit.HOURS);
	    Map.Entry<LocalDateTime, Boolean> entry = actividad.floorEntry(fecha);
	    if (entry != null) {
	        return entry.getValue();
	    }
	    return true; // Aca asumí que si no hay registro de actividad, siempre fue activo (porque la planta se inicia en true)
	}

}
