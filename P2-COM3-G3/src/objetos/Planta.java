package objetos;

import java.time.LocalDate;
import java.util.HashMap;

public abstract class Planta {
	private String nombre;
	private String direccion;
	private double capacidad;
	private boolean estado;
	private String fechaInicio;
	private HashMap<LocalDate, RegistroMonitoreo> monitoreos;
	public Planta(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio) {
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

	public HashMap<LocalDate, RegistroMonitoreo> getMonitoreos() {
		return monitoreos;
	}
	
}

