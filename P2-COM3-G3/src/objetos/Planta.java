package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Planta {
	protected String nombre;
	private String direccion;
	private double capacidad;
	private boolean estado;
	private String fechaInicio;
	protected HashMap<LocalDateTime, RegistroMonitoreo> monitoreos;
	public Planta(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio) {
		this.nombre = nombre;
		this.direccion = direccion;
		this.capacidad = capacidad;
		this.estado = estado;
		this.fechaInicio = fechaInicio;
		this.monitoreos = new LinkedHashMap<LocalDateTime, RegistroMonitoreo>();
	}

	public boolean cumpleCuota(String fecha, int hora) {
		return false;
	}
	
	public abstract double capacidadMinimaCuota();

	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fechaHora).truncatedTo(ChronoUnit.HOURS);
		if (this.monitoreos.containsKey(fechaHoraParseada)) {
			throw new RuntimeException("Ya hay un monitoreo para esta fecha + hora.");
		}
		RegistroMonitoreo registro = new RegistroMonitoreo(this.nombre, fechaHoraParseada, incidencias, energiaGenerada);
		monitoreos.put(fechaHoraParseada, registro);
		
	}
	
	public void activar(String fecha) {
		if (this.estado) {
			throw new RuntimeException("La planta ya esta activa");
		}
		this.estado = true;
	}
	public void desactivar(String fecha) {
		if (!this.estado) {
			throw new RuntimeException("La planta ya esta inactiva");
		}
		this.estado = false;
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
		
		LocalDate ultimoDia = Collections.max(this.monitoreos.keySet()).toLocalDate();
		
		
		for (Map.Entry<LocalDateTime, RegistroMonitoreo> entry : monitoreos.entrySet()) {
			if (entry.getKey().toLocalDate().equals(ultimoDia)) {
				int hora = entry.getKey().getHour();
				int incidencias = entry.getValue().getIncidencias();
				resultado[hora] = incidencias;
			}
		}
		return resultado;
	}
	
	public int[] historiogramaCumplimiento(String nombre, String fechaDesde, String fechaHasta) {
		LocalDate desde = LocalDate.parse(fechaDesde);
		LocalDate hasta = LocalDate.parse(fechaHasta);
		double capacidadMinimaCuota = capacidadMinimaCuota();
		int[] resultado = new int[24];
		
		for (Map.Entry<LocalDateTime, RegistroMonitoreo> entry : monitoreos.entrySet()) {
			LocalDateTime tiempoRegistro = entry.getKey();
			if (!tiempoRegistro.toLocalDate().isBefore(desde) && !tiempoRegistro.toLocalDate().isAfter(hasta)) {
				double generado = entry.getValue().getEnergiaGenerada();
				if (generado >= capacidadMinimaCuota) {
					resultado[tiempoRegistro.getHour()]+=1;
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

	public HashMap<LocalDateTime, RegistroMonitoreo> getMonitoreos() {
		return monitoreos;
	}

	@Override
	public String toString() {
		return "Planta [nombre=" + nombre + ", direccion=" + direccion + ", capacidad=" + capacidad + ", estado="
				+ estado + ", fechaInicio=" + fechaInicio + ", monitoreos=" + monitoreos + "]";
	}
	
	public String getDetalle() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.nombre + " ");
		sb.append(this.estado ? "(ACTIVA): " : "(INACTIVA): ");
		sb.append(this.fechaInicio);
		
		String sp = sb.toString();
		return sp;
	}
	
}

