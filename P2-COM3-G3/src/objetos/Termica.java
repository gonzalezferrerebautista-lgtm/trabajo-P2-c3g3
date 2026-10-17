package objetos;

import java.time.LocalDate;

public class Termica extends Planta {
	private String tipoCombustible;
	private double objetivoEmision;
	private double capacidadAlmacenamiento;
	public Termica(String nombre, String direccion, double capacidad, boolean estado, LocalDate fechaInicio,
			String tipoCombustible, double objetivoEmision, double capacidadAlmacenamiento) {
		super(nombre, direccion, capacidad, estado, fechaInicio);
		this.tipoCombustible = tipoCombustible;
		this.objetivoEmision = objetivoEmision;
		this.capacidadAlmacenamiento = capacidadAlmacenamiento;
	}
	
}
