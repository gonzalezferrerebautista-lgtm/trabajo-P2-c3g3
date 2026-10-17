package objetos;

import java.time.LocalDate;

public class Solar extends PlantaRenovable {
	private int cantidadPanelesInstalados;
	private double superficieCubierta;
	private double horasRadiacion;
	public Solar(String nombre, String direccion, double capacidad, boolean estado, LocalDate fechaInicio,
			double eficienciaPromedio, String fuentePrimaria, int cantidadPanelesInstalados, double superficieCubierta,
			double horasRadiacion) {
		super(nombre, direccion, capacidad, estado, fechaInicio, eficienciaPromedio, fuentePrimaria);
		this.cantidadPanelesInstalados = cantidadPanelesInstalados;
		this.superficieCubierta = superficieCubierta;
		this.horasRadiacion = horasRadiacion;
	}
	
}
