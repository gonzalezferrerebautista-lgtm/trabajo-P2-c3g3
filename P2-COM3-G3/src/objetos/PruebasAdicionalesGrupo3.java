package objetos;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Pruebas complementarias de corrección para el Grupo 3.
 * No modifica ninguna clase de los estudiantes ni los archivos oficiales.
 * Se ejecuta de forma autónoma con javac/java, o desde el envoltorio JUnit 4.
 * Cada prueba usa un sistema nuevo para evitar interdependencias.
 */
public final class PruebasAdicionalesGrupo3 {
    private static final String PUESTA = "2020-01-01T00:00:00";
    private static final String DIA = "2026-03-01";

    private PruebasAdicionalesGrupo3() {}

    private static ISmartGrids sistema() {
        ISmartGrids s = new SmartGrids();
        s.registrarPlantaSolar("Solar", "R1", 1000, PUESTA, "Sol", 0.5, 2000, 100, 12);
        s.registrarPlantaEolica("Eolica", "R2", 400, PUESTA, "Viento", 0.5, 5, 80, 5);
        s.registrarPlantaHidraulica("Hidro", "R3", 800, PUESTA, "Agua", 0.25, 4, 100);
        s.registrarPlantaTermica("Termica", "R4", 600, PUESTA, "Gas", 50, 600);
        return s;
    }

    private static void comprobar(boolean cond, String mensaje) {
        if (!cond) throw new AssertionError(mensaje);
    }
    private static void igual(int esperado, int actual, String mensaje) {
        comprobar(esperado == actual, mensaje + ": esperado=" + esperado + ", obtenido=" + actual);
    }
    private static void igual(double esperado, double actual, String mensaje) {
        comprobar(Math.abs(esperado - actual) < 1e-7, mensaje + ": esperado=" + esperado + ", obtenido=" + actual);
    }
    private static void debeRechazar(Runnable accion, String mensaje) {
        try {
            accion.run();
        } catch (RuntimeException esperado) {
            return;
        }
        throw new AssertionError(mensaje + " (no se lanzó ninguna excepción)");
    }

    // CASOS QUE DEBERÍAN SER CORRECTOS
    public static void cuotasCuatroTipos() {
        ISmartGrids s = sistema();
        igual(250, s.capacidadMinimaCuota("Solar"), "Cuota solar");
        igual(200, s.capacidadMinimaCuota("Eolica"), "Cuota eólica");
        igual(200, s.capacidadMinimaCuota("Hidro"), "Cuota hidráulica");
        igual(300, s.capacidadMinimaCuota("Termica"), "Cuota térmica");
    }
    public static void noDuplicaMonitoreosEnMismaHora() {
        ISmartGrids s = sistema();
        s.registrarMonitoreo("Solar", DIA+"T10:05:00", 300, 0);
        debeRechazar(() -> s.registrarMonitoreo("Solar", DIA+"T10:45:00", 100, 0),
                "Dos registros en la misma hora deben ser rechazados");
    }
    public static void historialBasicoDeInactividad() {
        ISmartGrids s = sistema();
        s.desactivarPlanta("Solar", DIA+"T09:00:00");
        s.activarPlanta("Solar", DIA+"T11:00:00");
        comprobar(s.plantasInactivasEn(DIA+"T10:00:00").contains("Solar"),
                "La planta debe constar como inactiva en el intervalo");
        comprobar(!s.plantasInactivasEn(DIA+"T12:00:00").contains("Solar"),
                "La planta debe constar como activa tras la reactivación");
    }
    public static void histogramaFechasInclusivas() {
        ISmartGrids s = sistema();
        s.registrarMonitoreo("Solar", DIA+"T12:00:00", 250, 0);
        s.registrarMonitoreo("Solar", "2026-03-02T12:00:00", 250, 0);
        igual(2, s.histogramaCumplimiento("Solar", DIA, "2026-03-02")[12],
                "Se cuentan ambos extremos");
        igual(1, s.histogramaCumplimiento("Solar", DIA, DIA)[12], "Un solo día");
    }
    public static void fuentesRenovablesOrdenadas() {
        ISmartGrids s = sistema();
        Map<String, Integer> mapa = s.plantasRenovablesPorFuente();
        Iterator<String> claves = mapa.keySet().iterator();
        comprobar(claves.hasNext() && "Agua".equals(claves.next()), "Primera fuente debe ser Agua");
        comprobar(claves.hasNext() && "Sol".equals(claves.next()), "Segunda fuente debe ser Sol");
        comprobar(claves.hasNext() && "Viento".equals(claves.next()), "Tercera fuente debe ser Viento");
    }
    public static void vientoInsuficienteSoloSiNuncaSeAlcanzo() {
        ISmartGrids s = sistema();
        s.registrarMonitoreo("Eolica", DIA+"T01:00:00", 10, 0, 2);
        s.registrarMonitoreo("Eolica", "2026-03-02T01:00:00", 10, 0, 6);
        s.registrarMonitoreo("Eolica", DIA+"T02:00:00", 10, 0, 4);
        String r = s.horasSinVelocidadMinimaPorPlantaEolica();
        comprobar(r.contains("02:"), "Debe figurar la hora 02 (siempre con poco viento)");
        comprobar(!r.contains("01:"), "No debe figurar hora 01 (alguna vez hubo suficiente viento)");
    }
    public static void limpiezaConIncidenciasDistintasDeCero() {
        ISmartGrids s = sistema();
        s.registrarMonitoreo("Solar", DIA+"T01:00:00", 0, 0);
        s.registrarMonitoreo("Solar", DIA+"T02:00:00", 0, 1);
        s.registrarMonitoreo("Solar", DIA+"T03:00:00", 10, 0);
        igual(1, s.limpiarMonitoreosSinDatos(), "Sólo se elimina registro totalmente vacío");
        igual(10, s.totalGenerado("Solar"), "Total tras limpieza");
        igual(1, s.incidenciasPorHora("Solar")[2], "Las incidencias se conservan");
    }
    public static void rechazaTipoIncorrectoDeMonitoreo() {
        ISmartGrids s = sistema();
        debeRechazar(() -> s.registrarMonitoreo("Eolica", DIA+"T10:00:00", 100, 0),
                "Eólica necesita velocidad de viento");
        debeRechazar(() -> s.registrarMonitoreo("Termica", DIA+"T10:00:00", 100, 0),
                "Térmica necesita CO2 y combustible");
    }

    // SITUACIONES LÍMITE: deben respetar el contrato público / IREP
    public static void noAdmiteMonitoreoSiActualmenteEstaInactiva() {
        ISmartGrids s = sistema();
        s.desactivarPlanta("Solar", DIA+"T12:00:00");
        debeRechazar(() -> s.registrarMonitoreo("Solar", DIA+"T10:00:00", 50, 0),
                "La interfaz rechaza registrar monitoreos cuando la planta está inactiva");
    }
    public static void noPermiteDesactivarEolicaAntesDeMonitoreoExistente() {
        ISmartGrids s = sistema();
        s.registrarMonitoreo("Eolica", DIA+"T12:00:00", 50, 0, 4);
        debeRechazar(() -> s.desactivarPlanta("Eolica", DIA+"T10:00:00"),
                "Desactivación retroactiva deja monitoreo eólico en período inactivo");
    }
    public static void noPermiteDesactivarTermicaAntesDeMonitoreoExistente() {
        ISmartGrids s = sistema();
        s.registrarMonitoreo("Termica", DIA+"T12:00:00", 300, 0, 10, 2);
        debeRechazar(() -> s.desactivarPlanta("Termica", DIA+"T10:00:00"),
                "Desactivación retroactiva deja monitoreo térmico en período inactivo");
    }
    public static void noAceptaMonitoreoAnteriorAInicioExacto() {
        ISmartGrids s = new SmartGrids();
        s.registrarPlantaSolar("Nueva", "R1", 100, DIA+"T10:30:00", "Sol", 0.5, 100, 10, 12);
        debeRechazar(() -> s.registrarMonitoreo("Nueva", DIA+"T10:00:00", 10, 0),
                "El monitoreo es anterior al inicio real, aunque cae en la misma hora");
    }
    public static void desactivacionConMinutosNoAfectaMinutosPrevios() {
        ISmartGrids s = sistema();
        s.desactivarPlanta("Solar", DIA+"T10:30:00");
        comprobar(!s.plantasInactivasEn(DIA+"T10:00:00").contains("Solar"),
                "A las 10:00 seguía activa, aunque se desactivó a las 10:30");
    }
    public static void activacionConMinutosNoAfectaMinutosPrevios() {
        ISmartGrids s = sistema();
        s.desactivarPlanta("Solar", DIA+"T09:00:00");
        s.activarPlanta("Solar", DIA+"T11:30:00");
        comprobar(s.plantasInactivasEn(DIA+"T11:00:00").contains("Solar"),
                "A las 11:00 seguía inactiva, aunque se activó a las 11:30");
    }
    public static void rechazaEnergiaNaN() {
        ISmartGrids s = sistema();
        debeRechazar(() -> s.registrarMonitoreo("Solar", DIA+"T10:00:00", Double.NaN, 0),
                "La energía NaN no es un valor válido");
    }
    public static void rechazaEnergiaInfinita() {
        ISmartGrids s = sistema();
        debeRechazar(() -> s.registrarMonitoreo("Solar", DIA+"T10:00:00", Double.POSITIVE_INFINITY, 0),
                "La energía infinita no es un valor válido");
    }
    public static void rechazaCapacidadInfinita() {
        ISmartGrids s = new SmartGrids();
        debeRechazar(() -> s.registrarPlantaTermica("T", "R", Double.POSITIVE_INFINITY, PUESTA, "Gas", 20, 200),
                "La capacidad máxima debe ser un número finito");
    }
    public static void rechazaEficienciaNaN() {
        ISmartGrids s = new SmartGrids();
        debeRechazar(() -> s.registrarPlantaEolica("E", "R", 100, PUESTA, "Viento", Double.NaN, 3, 80, 5),
                "La eficiencia debe ser un número finito");
    }
    public static void rechazaVelocidadVientoInfinita() {
        ISmartGrids s = sistema();
        debeRechazar(() -> s.registrarMonitoreo("Eolica", DIA+"T10:00:00", 100, 0, Double.POSITIVE_INFINITY),
                "La velocidad del viento debe ser finita");
    }
    public static void rechazaCO2Infinito() {
        ISmartGrids s = sistema();
        debeRechazar(() -> s.registrarMonitoreo("Termica", DIA+"T10:00:00", 300, 0, Double.POSITIVE_INFINITY, 10),
                "El CO2 generado debe ser finito");
    }
    public static void rechazaCombustibleNaN() {
        ISmartGrids s = sistema();
        debeRechazar(() -> s.registrarMonitoreo("Termica", DIA+"T10:00:00", 300, 0, 20, Double.NaN),
                "El combustible utilizado debe ser finito");
    }

    private interface Caso { void ejecutar(); }
    private static int total = 0, ok = 0;
    private static void correr(String nombre, Caso caso) {
        total++;
        try { caso.ejecutar(); ok++; System.out.println("OK    " + nombre); }
        catch (Throwable e) { System.out.println("FALLA " + nombre + " — " + e); }
    }
    public static void main(String[] args) {
        correr("Cuotas por tecnología", PruebasAdicionalesGrupo3::cuotasCuatroTipos);
        correr("Duplicado en una misma hora", PruebasAdicionalesGrupo3::noDuplicaMonitoreosEnMismaHora);
        correr("Historial básico de inactividad", PruebasAdicionalesGrupo3::historialBasicoDeInactividad);
        correr("Histograma de fechas inclusivas", PruebasAdicionalesGrupo3::histogramaFechasInclusivas);
        correr("Fuentes renovables ordenadas", PruebasAdicionalesGrupo3::fuentesRenovablesOrdenadas);
        correr("Horas con viento insuficiente", PruebasAdicionalesGrupo3::vientoInsuficienteSoloSiNuncaSeAlcanzo);
        correr("Limpieza de registros vacíos", PruebasAdicionalesGrupo3::limpiezaConIncidenciasDistintasDeCero);
        correr("Rechazo de tipos incorrectos", PruebasAdicionalesGrupo3::rechazaTipoIncorrectoDeMonitoreo);
        correr("No monitorear cuando está inactiva", PruebasAdicionalesGrupo3::noAdmiteMonitoreoSiActualmenteEstaInactiva);
        correr("No desactivar eólica retroactivamente", PruebasAdicionalesGrupo3::noPermiteDesactivarEolicaAntesDeMonitoreoExistente);
        correr("No desactivar térmica retroactivamente", PruebasAdicionalesGrupo3::noPermiteDesactivarTermicaAntesDeMonitoreoExistente);
        correr("Monitoreo no anterior a puesta en marcha", PruebasAdicionalesGrupo3::noAceptaMonitoreoAnteriorAInicioExacto);
        correr("Precisión horaria de desactivación", PruebasAdicionalesGrupo3::desactivacionConMinutosNoAfectaMinutosPrevios);
        correr("Precisión horaria de activación", PruebasAdicionalesGrupo3::activacionConMinutosNoAfectaMinutosPrevios);
        correr("Rechaza energía NaN", PruebasAdicionalesGrupo3::rechazaEnergiaNaN);
        correr("Rechaza energía infinita", PruebasAdicionalesGrupo3::rechazaEnergiaInfinita);
        correr("Rechaza capacidad infinita", PruebasAdicionalesGrupo3::rechazaCapacidadInfinita);
        correr("Rechaza eficiencia NaN", PruebasAdicionalesGrupo3::rechazaEficienciaNaN);
        correr("Rechaza viento infinito", PruebasAdicionalesGrupo3::rechazaVelocidadVientoInfinita);
        correr("Rechaza CO2 infinito", PruebasAdicionalesGrupo3::rechazaCO2Infinito);
        correr("Rechaza combustible NaN", PruebasAdicionalesGrupo3::rechazaCombustibleNaN);
        System.out.println("RESULTADO: " + ok + "/" + total + " pruebas adicionales correctas; " + (total-ok) + " fallos detectados.");
        // No System.exit(1): se permite revisar los resultados sin cerrar la ventana.
    }
}
