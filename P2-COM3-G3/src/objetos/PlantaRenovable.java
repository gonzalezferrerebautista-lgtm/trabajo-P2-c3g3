package objetos;

import java.time.LocalDate;

public class PlantaRenovable extends Planta {
	private double eficienciaPromedio;
	private String fuentePrimaria;
	public PlantaRenovable(String nombre, String direccion, double capacidad, boolean estado, LocalDate fechaInicio,
			double eficienciaPromedio, String fuentePrimaria) {
		super(nombre, direccion, capacidad, estado, fechaInicio);
		this.eficienciaPromedio = eficienciaPromedio;
		this.fuentePrimaria = fuentePrimaria;
	}
	
}
