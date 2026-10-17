package objetos;

import java.time.LocalDate;

public class Eolica extends PlantaRenovable {
	private int cantidadAerogeneradores;
	private double alturaTorre;
	private double velocidadMinViento;
	public Eolica(String nombre, String direccion, double capacidad, boolean estado, LocalDate fechaInicio,
			double eficienciaPromedio, String fuentePrimaria, int cantidadAerogeneradores, double alturaTorre,
			double velocidadMinViento) {
		super(nombre, direccion, capacidad, estado, fechaInicio, eficienciaPromedio, fuentePrimaria);
		this.cantidadAerogeneradores = cantidadAerogeneradores;
		this.alturaTorre = alturaTorre;
		this.velocidadMinViento = velocidadMinViento;
	}
}
