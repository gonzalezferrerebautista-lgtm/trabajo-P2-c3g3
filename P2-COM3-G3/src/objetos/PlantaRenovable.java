package objetos;

import java.time.LocalDate;

public class PlantaRenovable extends Planta {
	private double eficienciaPromedio;
	private String fuentePrimaria;
	public PlantaRenovable(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio,
			double eficienciaPromedio, String fuentePrimaria) {
		super(nombre, direccion, capacidad, estado, fechaInicio);
		this.eficienciaPromedio = eficienciaPromedio;
		this.fuentePrimaria = fuentePrimaria;
	}
	public double getEficienciaPromedio() {
		return eficienciaPromedio;
	}
	public String getFuentePrimaria() {
		return fuentePrimaria;
	}
	@Override
	public double capacidadMinimaCuota() {
		return super.getCapacidad()*eficienciaPromedio;
	}
	
}
