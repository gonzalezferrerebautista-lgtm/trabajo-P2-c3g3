package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Hidraulica extends PlantaRenovable {
	private int cantidadTurbinas;
	private double capacidadEmbalse;

	public Hidraulica(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio,
			double eficienciaPromedio, String fuentePrimaria, int cantidadTurbinas, double capacidadEmbalse) {
		super(nombre, direccion, capacidad, estado, fechaInicio, eficienciaPromedio, fuentePrimaria);
		this.cantidadTurbinas = cantidadTurbinas;
		this.capacidadEmbalse = capacidadEmbalse;
	}

}
