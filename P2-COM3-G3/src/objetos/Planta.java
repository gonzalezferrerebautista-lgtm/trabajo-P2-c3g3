package objetos;

import java.time.LocalDate;
import java.util.HashMap;

public abstract class Planta {
	private String nombre;
	private String direccion;
	private double capacidad;
	private boolean estado;
	private LocalDate fechaInicio;
	private HashMap<LocalDate, RegistroMonitoreo> monitoreos;
	public Planta(String nombre, String direccion, double capacidad, boolean estado, LocalDate fechaInicio) {
		this.nombre = nombre;
		this.direccion = direccion;
		this.capacidad = capacidad;
		this.estado = estado;
		this.fechaInicio = fechaInicio;
		this.monitoreos = new HashMap<LocalDate, RegistroMonitoreo>();
	}

	public boolean cumpleCuota(LocalDate fecha, int hora) {
		return false;
	}
	
	public void registrarMonitoreo(LocalDate fecha, int hora, int incidencias, double energiaGenerada) {
		
	}
	
}

