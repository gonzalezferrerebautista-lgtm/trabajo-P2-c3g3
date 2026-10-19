package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Solar extends PlantaRenovable {
	private int cantidadPanelesInstalados;
	private double superficieCubierta;
	private double horasRadiacion;

	public Solar(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio,
			double eficienciaPromedio, String fuentePrimaria, int cantidadPanelesInstalados, double superficieCubierta,
			double horasRadiacion) {
		super(nombre, direccion, capacidad, estado, fechaInicio, eficienciaPromedio, fuentePrimaria);
		this.cantidadPanelesInstalados = cantidadPanelesInstalados;
		this.superficieCubierta = superficieCubierta;
		this.horasRadiacion = horasRadiacion;
	}

	@Override
	public boolean cumpleCuota(String fecha, int hora) {
		LocalDateTime clave = LocalDate.parse(fecha).atTime(hora, 0);
		RegistroMonitoreo r = monitoreos.get(clave);
		if (r == null) {
			return false;
		}
		double cuota = getCapacidad() * getEficienciaPromedio() * horasRadiacion / 24;
		return r.getEnergiaGenerada() >= cuota;
	}

	public int getCantidadPanelesInstalados() {
		return cantidadPanelesInstalados;
	}

	public double getSuperficieCubierta() {
		return superficieCubierta;
	}

	public double getHorasRadiacion() {
		return horasRadiacion;
	}

	@Override
	public double capacidadMinimaCuota() {
		return super.capacidadMinimaCuota() * (horasRadiacion / 24);
	}
}
