package es.um.dis.formatOquo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

class ScalesUtilsTest {

	private static final String OQUO = OWLUtils.OQUO_NS;
	private static final String OM = OWLUtils.OM_NS;
	private static final String EX = "http://example.org/test#";

	private static final String HEADER = row("metric", "input_scale", "output_scale", "1", "2", "3", "4", "5", "Notas");

	private static final String NAMES_METRIC = OQUO + "NamesPerPropertyMetric";
	private static final String NAMES_SCALE = OQUO + "NamesPerPropertyMetricScale";
	private static final String OQUARE_SCALE = OQUO + "OquareFiveCategoriesScale";
	private static final String NAMES_ROW = row(NAMES_METRIC, NAMES_SCALE, OQUARE_SCALE,
			"[0, 0.1)", "[0.1, 0.97)", "[0.97, 1.03)", "[1.03, 1.5)", "[1.5, Inf)", "");

	private static final ScaleCreationConfiguration NO_FLAGS = new ScaleCreationConfiguration(false, false, false);
	private static final ScaleCreationConfiguration OVERWRITE = new ScaleCreationConfiguration(true, false, false);
	private static final ScaleCreationConfiguration CREATE_ALL = new ScaleCreationConfiguration(false, true, true);

	@TempDir
	Path tempDir;

	@Test
	void createsCardinalToOrdinalConversionForRatioScale() throws Exception {
		OWLOntology result = run(NO_FLAGS, NAMES_ROW);

		OWLNamedIndividual conversion = single(newConversions(result, NAMES_SCALE));
		assertTrue(hasType(result, conversion, OQUO + "CardinalToOrdinalScaleConversion"));
		assertEquals(Set.of(IRI.create(OQUARE_SCALE)), objectValues(result, conversion, OWLUtils.HAS_OUTPUT_SCALE_IRI));

		Map<IRI, OWLNamedIndividual> mappingsByCategory = mappingsByOutputCategory(result, conversion);
		assertEquals(5, mappingsByCategory.size());
		for (OWLNamedIndividual mapping : mappingsByCategory.values()) {
			assertTrue(hasType(result, mapping, OWLUtils.CARDINAL_TO_ORDINAL_SCALE_POINT_MAPPING_IRI));
		}

		OWLNamedIndividual interval3 = single(objectValuesAsIndividuals(result,
				mappingsByCategory.get(IRI.create(OQUO + "OquareCategory3")), OWLUtils.HAS_INPUT_CARDINAL_INTERVAL_IRI));
		assertTrue(hasType(result, interval3, OWLUtils.INTERVAL_IRI));
		assertEquals("0.97", dataValue(result, interval3, OWLUtils.HAS_INTERVAL_LOWER_VALUE_IRI));
		assertEquals("1.03", dataValue(result, interval3, OWLUtils.HAS_INTERVAL_UPPER_VALUE_IRI));
		assertEquals(Set.of(IRI.create(OWLUtils.CLOSED_BOUND_TYPE_IRI)),
				objectValues(result, interval3, OWLUtils.HAS_INTERVAL_LOWER_BOUND_TYPE_IRI));
		assertEquals(Set.of(IRI.create(OWLUtils.OPEN_BOUND_TYPE_IRI)),
				objectValues(result, interval3, OWLUtils.HAS_INTERVAL_UPPER_BOUND_TYPE_IRI));
	}

	@Test
	void writesInfiniteUpperBoundAsINF() throws Exception {
		OWLOntology result = run(NO_FLAGS, NAMES_ROW);

		OWLNamedIndividual conversion = single(newConversions(result, NAMES_SCALE));
		OWLNamedIndividual interval5 = single(objectValuesAsIndividuals(result,
				mappingsByOutputCategory(result, conversion).get(IRI.create(OQUO + "OquareCategory5")),
				OWLUtils.HAS_INPUT_CARDINAL_INTERVAL_IRI));
		assertEquals("1.5", dataValue(result, interval5, OWLUtils.HAS_INTERVAL_LOWER_VALUE_IRI));
		assertEquals("INF", dataValue(result, interval5, OWLUtils.HAS_INTERVAL_UPPER_VALUE_IRI));
	}

	@Test
	void keepsExistingConversionWithoutOverwrite() throws Exception {
		OWLOntology result = run(NO_FLAGS, NAMES_ROW);

		Set<OWLNamedIndividual> conversions = conversionsWithInputScale(result, NAMES_SCALE);
		assertEquals(2, conversions.size());
		assertTrue(conversions.contains(individual(OQUO + "scaleConversion-existing")));
	}

	@Test
	void overwriteReplacesExistingConversion() throws Exception {
		OWLOntology result = run(OVERWRITE, NAMES_ROW);

		Set<OWLNamedIndividual> conversions = conversionsWithInputScale(result, NAMES_SCALE);
		assertEquals(1, conversions.size());
		assertFalse(result.containsIndividualInSignature(IRI.create(OQUO + "scaleConversion-existing")));
		assertFalse(result.containsIndividualInSignature(IRI.create(OQUO + "scalePointMapping-existing")));
		assertFalse(result.containsIndividualInSignature(IRI.create(OQUO + "interval-existing")));
	}

	@Test
	void skipsRowWhenMetricIsMissingAndNotCreated() throws Exception {
		String metric = OQUO + "UnknownMetric";
		OWLOntology result = run(NO_FLAGS, row(metric, NAMES_SCALE, OQUARE_SCALE,
				"[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", ""));

		assertTrue(newConversions(result, NAMES_SCALE).isEmpty());
		assertFalse(result.containsClassInSignature(IRI.create(metric)));
	}

	@Test
	void skipsRowWhenInputScaleIsMissingAndNotCreated() throws Exception {
		String scale = OQUO + "UnknownScale";
		OWLOntology result = run(NO_FLAGS, row(NAMES_METRIC, scale, OQUARE_SCALE,
				"[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", ""));

		assertTrue(conversionsWithInputScale(result, scale).isEmpty());
		assertFalse(result.containsIndividualInSignature(IRI.create(scale)));
	}

	@Test
	void createsMissingEntitiesWhenRequested() throws Exception {
		String metric = OQUO + "NewMetric";
		String scale = OQUO + "NewMetricScale";
		OWLOntology result = run(CREATE_ALL, row(metric, scale, OQUARE_SCALE,
				"[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", ""));

		OWLDataFactory df = result.getOWLOntologyManager().getOWLDataFactory();
		assertTrue(result.containsClassInSignature(IRI.create(metric)));
		assertTrue(result.containsAxiom(df.getOWLSubClassOfAxiom(df.getOWLClass(metric),
				df.getOWLClass(OWLUtils.QM_NS + "DerivedMeasure"))));
		assertTrue(hasType(result, individual(scale), OWLUtils.RAW_SCALE_IRI));
		assertTrue(hasType(result, individual(scale), OM + "Ratio_scale"));

		OWLNamedIndividual conversion = single(conversionsWithInputScale(result, scale));
		assertEquals(5, mappingsByOutputCategory(result, conversion).size());
	}

	@Test
	void skipsRatioRowWithEmptyRange() throws Exception {
		OWLOntology result = run(NO_FLAGS, row(NAMES_METRIC, NAMES_SCALE, OQUARE_SCALE,
				"[0, 0.1)", "", "[0.97, 1.03)", "[1.03, 1.5)", "[1.5, Inf)", ""));

		assertTrue(newConversions(result, NAMES_SCALE).isEmpty());
	}

	@Test
	void processesSeveralRows() throws Exception {
		String metric = OQUO + "OtherMetric";
		String scale = OQUO + "OtherMetricScale";
		OWLOntology result = run(CREATE_ALL, NAMES_ROW, row(metric, scale, OQUARE_SCALE,
				"[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", "some notes"));

		assertEquals(1, newConversions(result, NAMES_SCALE).size());
		assertEquals(1, conversionsWithInputScale(result, scale).size());
	}

	@Test
	void createsOrdinalToOrdinalConversionForOrdinalScale() throws Exception {
		String scale = EX + "LikertScale";
		OWLOntology result = run(NO_FLAGS, row(EX + "LikertMetric", scale, OQUARE_SCALE,
				EX + "Low", "", EX + "Medium", "", EX + "High", ""));

		OWLNamedIndividual conversion = single(conversionsWithInputScale(result, scale));
		assertTrue(hasType(result, conversion, OQUO + "OrdinalToOrdinalScaleConversion"));
		assertEquals(Set.of(IRI.create(OQUARE_SCALE)), objectValues(result, conversion, OWLUtils.HAS_OUTPUT_SCALE_IRI));

		Map<IRI, OWLNamedIndividual> mappingsByCategory = mappingsByOutputCategory(result, conversion);
		assertEquals(Set.of(IRI.create(OQUO + "OquareCategory1"), IRI.create(OQUO + "OquareCategory3"),
				IRI.create(OQUO + "OquareCategory5")), mappingsByCategory.keySet());
		for (OWLNamedIndividual mapping : mappingsByCategory.values()) {
			assertTrue(hasType(result, mapping, OWLUtils.ORDINAL_TO_ORDINAL_SCALE_POINT_MAPPING_IRI));
		}
		assertEquals(Set.of(IRI.create(EX + "Low")), objectValues(result,
				mappingsByCategory.get(IRI.create(OQUO + "OquareCategory1")), OWLUtils.HAS_INPUT_SCALE_CATEGORY_IRI));
		assertEquals(Set.of(IRI.create(EX + "Medium")), objectValues(result,
				mappingsByCategory.get(IRI.create(OQUO + "OquareCategory3")), OWLUtils.HAS_INPUT_SCALE_CATEGORY_IRI));
		assertEquals(Set.of(IRI.create(EX + "High")), objectValues(result,
				mappingsByCategory.get(IRI.create(OQUO + "OquareCategory5")), OWLUtils.HAS_INPUT_SCALE_CATEGORY_IRI));
	}

	@Test
	void createFlagsDoNotModifyExistingEntities() throws Exception {
		String metric = EX + "LikertMetric";
		String scale = EX + "LikertScale";
		OWLOntology result = run(CREATE_ALL, row(metric, scale, OQUARE_SCALE,
				EX + "Low", "", EX + "Medium", "", EX + "High", ""));

		OWLDataFactory df = result.getOWLOntologyManager().getOWLDataFactory();
		assertFalse(result.containsAxiom(df.getOWLSubClassOfAxiom(df.getOWLClass(metric),
				df.getOWLClass(OWLUtils.QM_NS + "DerivedMeasure"))));
		assertFalse(hasType(result, individual(scale), OWLUtils.RAW_SCALE_IRI));
		assertFalse(hasType(result, individual(scale), OM + "Ratio_scale"));
		assertEquals(1, result.classAssertionAxioms(individual(OQUARE_SCALE)).count());

		OWLNamedIndividual conversion = single(conversionsWithInputScale(result, scale));
		assertTrue(hasType(result, conversion, OQUO + "OrdinalToOrdinalScaleConversion"));
	}

	@Test
	void skipsRowWhoseOutputScaleIsNotTheOquareScale() throws Exception {
		String outputScale = EX + "LikertScale";
		OWLOntology result = run(OVERWRITE, row(NAMES_METRIC, NAMES_SCALE, outputScale,
				"[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", ""));

		assertTrue(newConversions(result, NAMES_SCALE).isEmpty());
		assertTrue(result.containsIndividualInSignature(IRI.create(OQUO + "scaleConversion-existing")));
	}

	@Test
	void skipsRowWhoseOutputScaleIsMissingEvenWithCreateFlags() throws Exception {
		String outputScale = OQUO + "NewOutputScale";
		OWLOntology result = run(CREATE_ALL, row(NAMES_METRIC, NAMES_SCALE, outputScale,
				"[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", ""));

		assertTrue(newConversions(result, NAMES_SCALE).isEmpty());
		assertFalse(result.containsIndividualInSignature(IRI.create(outputScale)));
	}

	@Test
	void findsMetricAndScalesDefinedInImports() throws Exception {
		File module = tempDir.resolve("importing-module.ttl").toFile();
		Files.writeString(module.toPath(), String.format("""
				@prefix owl: <http://www.w3.org/2002/07/owl#> .
				<http://example.org/importing-module> a owl:Ontology ;
				    owl:imports <%s> .
				""", fixture().toURI()));

		OWLOntology result = run(module, NO_FLAGS, NAMES_ROW,
				row(EX + "LikertMetric", EX + "LikertScale", OQUARE_SCALE, EX + "Low", "", "", "", EX + "High", ""));

		assertEquals(1, newConversions(result, NAMES_SCALE).size());
		assertEquals(1, conversionsWithInputScale(result, EX + "LikertScale").size());
	}

	@Test
	void declaresConversionsAndPointMappings() throws Exception {
		OWLOntology result = run(NO_FLAGS, NAMES_ROW,
				row(EX + "LikertMetric", EX + "LikertScale", OQUARE_SCALE, EX + "Low", "", "", "", EX + "High", ""));

		OWLDataFactory df = result.getOWLOntologyManager().getOWLDataFactory();
		Set<OWLNamedIndividual> conversions = new java.util.HashSet<>(newConversions(result, NAMES_SCALE));
		conversions.addAll(conversionsWithInputScale(result, EX + "LikertScale"));
		assertEquals(2, conversions.size());
		for (OWLNamedIndividual conversion : conversions) {
			assertTrue(result.containsAxiom(df.getOWLDeclarationAxiom(conversion)), "Undeclared " + conversion);
			for (OWLNamedIndividual mapping : mappingsByOutputCategory(result, conversion).values()) {
				assertTrue(result.containsAxiom(df.getOWLDeclarationAxiom(mapping)), "Undeclared " + mapping);
			}
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "[0)", "[a, 1)", "[2, 1)" })
	void invalidRangeDoesNotRemoveExistingConversion(String invalidRange) throws Exception {
		OWLOntology result = run(OVERWRITE, row(NAMES_METRIC, NAMES_SCALE, OQUARE_SCALE,
				"[0, 0.1)", invalidRange, "[0.97, 1.03)", "[1.03, 1.5)", "[1.5, Inf)", ""));

		assertEquals(Set.of(individual(OQUO + "scaleConversion-existing")), conversionsWithInputScale(result, NAMES_SCALE));
		assertTrue(result.containsIndividualInSignature(IRI.create(OQUO + "scalePointMapping-existing")));
		assertTrue(result.containsIndividualInSignature(IRI.create(OQUO + "interval-existing")));
	}

	@Test
	void invalidRowDoesNotStopTheRemainingRows() throws Exception {
		String metric = OQUO + "OtherMetric";
		String scale = OQUO + "OtherMetricScale";
		OWLOntology result = run(CREATE_ALL,
				row(NAMES_METRIC, NAMES_SCALE, OQUARE_SCALE, "[0, 0.1)", "[a, 1)", "[0.97, 1.03)", "[1.03, 1.5)", "[1.5, Inf)", ""),
				row(metric, scale, OQUARE_SCALE, "[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", ""));

		assertTrue(newConversions(result, NAMES_SCALE).isEmpty());
		assertEquals(1, conversionsWithInputScale(result, scale).size());
	}

	@Test
	void invalidRowDoesNotCreateEntities() throws Exception {
		String metric = OQUO + "NewMetric";
		String scale = OQUO + "NewMetricScale";
		OWLOntology result = run(CREATE_ALL, row(metric, scale, OQUARE_SCALE,
				"[0, 1)", "[1, 2)", "[a, 3)", "[3, 4)", "[4, Inf)", ""));

		assertFalse(result.containsClassInSignature(IRI.create(metric)));
		assertFalse(result.containsIndividualInSignature(IRI.create(scale)));
	}

	@Test
	void inputScaleThatIsNeitherRatioNorOrdinalDoesNotRemoveExistingConversion() throws Exception {
		File module = tempDir.resolve("module-with-nominal-scale.ttl").toFile();
		Files.writeString(module.toPath(), String.format("""
				@prefix owl: <http://www.w3.org/2002/07/owl#> .
				@prefix om: <http://www.wurvoc.org/vocabularies/om-1.8/> .
				@prefix oquo: <https://purl.org/oquo#> .
				<http://example.org/module-with-nominal-scale> a owl:Ontology ;
				    owl:imports <%s> .
				om:Nominal_scale a owl:Class .
				oquo:NominalScale a owl:NamedIndividual, om:Nominal_scale .
				oquo:scaleConversion-nominal a owl:NamedIndividual, oquo:CardinalToOrdinalScaleConversion ;
				    oquo:hasInputScale oquo:NominalScale ;
				    oquo:hasOutputScale oquo:OquareFiveCategoriesScale .
				""", fixture().toURI()));

		OWLOntology result = run(module, OVERWRITE, row(NAMES_METRIC, OQUO + "NominalScale", OQUARE_SCALE,
				"[0, 1)", "[1, 2)", "[2, 3)", "[3, 4)", "[4, Inf)", ""));

		assertEquals(Set.of(individual(OQUO + "scaleConversion-nominal")),
				conversionsWithInputScale(result, OQUO + "NominalScale"));
	}

	@Test
	void skipsRowsWithTooFewColumns() throws Exception {
		OWLOntology result = run(NO_FLAGS, "", "\"" + NAMES_METRIC + "\"", NAMES_ROW);

		assertEquals(1, newConversions(result, NAMES_SCALE).size());
	}

	// Helpers

	private OWLOntology run(ScaleCreationConfiguration config, String... rows) throws Exception {
		return run(fixture(), config, rows);
	}

	private OWLOntology run(File inputOntology, ScaleCreationConfiguration config, String... rows) throws Exception {
		File tsv = tempDir.resolve("scales.tsv").toFile();
		Files.writeString(tsv.toPath(), HEADER + "\n" + String.join("\n", rows) + "\n");
		File output = tempDir.resolve("output.ttl").toFile();

		ScalesUtils.generateScales(IRI.create(inputOntology), output, tsv, config);

		return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(output);
	}

	private static File fixture() throws URISyntaxException, IOException {
		return new File(ScalesUtilsTest.class.getResource("/test-module.ttl").toURI());
	}

	private static String row(String... fields) {
		return Arrays.stream(fields).map(f -> "\"" + f + "\"").collect(Collectors.joining("\t"));
	}

	private static OWLNamedIndividual individual(String iri) {
		return OWLManager.getOWLDataFactory().getOWLNamedIndividual(iri);
	}

	private static <T> T single(Set<T> elements) {
		assertEquals(1, elements.size(), "Expected exactly one element but got " + elements);
		return elements.iterator().next();
	}

	private static boolean hasType(OWLOntology ontology, OWLNamedIndividual individual, String classIRI) {
		return ontology.classAssertionAxioms(individual)
				.map(OWLClassAssertionAxiom::getClassExpression)
				.anyMatch(ce -> !ce.isAnonymous() && ce.asOWLClass().getIRI().equals(IRI.create(classIRI)));
	}

	private static Set<OWLNamedIndividual> objectValuesAsIndividuals(OWLOntology ontology, OWLNamedIndividual subject,
			String propertyIRI) {
		return ontology.objectPropertyAssertionAxioms(subject)
				.filter(ax -> ax.getProperty().asOWLObjectProperty().getIRI().equals(IRI.create(propertyIRI)))
				.map(OWLObjectPropertyAssertionAxiom::getObject)
				.map(o -> o.asOWLNamedIndividual())
				.collect(Collectors.toSet());
	}

	private static Set<IRI> objectValues(OWLOntology ontology, OWLNamedIndividual subject, String propertyIRI) {
		return objectValuesAsIndividuals(ontology, subject, propertyIRI).stream()
				.map(OWLNamedIndividual::getIRI)
				.collect(Collectors.toSet());
	}

	private static String dataValue(OWLOntology ontology, OWLNamedIndividual subject, String propertyIRI) {
		List<String> values = ontology.dataPropertyAssertionAxioms(subject)
				.filter(ax -> ax.getProperty().asOWLDataProperty().getIRI().equals(IRI.create(propertyIRI)))
				.map(OWLDataPropertyAssertionAxiom::getObject)
				.map(literal -> literal.getLiteral())
				.collect(Collectors.toList());
		assertEquals(1, values.size(), "Expected exactly one value for " + propertyIRI + " but got " + values);
		return values.get(0);
	}

	private static Set<OWLNamedIndividual> conversionsWithInputScale(OWLOntology ontology, String inputScaleIRI) {
		return ontology.axioms(org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_ASSERTION)
				.filter(ax -> ax.getProperty().asOWLObjectProperty().getIRI().equals(IRI.create(OWLUtils.HAS_INPUT_SCALE_IRI)))
				.filter(ax -> ax.getObject().isNamed()
						&& ax.getObject().asOWLNamedIndividual().getIRI().equals(IRI.create(inputScaleIRI)))
				.map(ax -> ax.getSubject().asOWLNamedIndividual())
				.collect(Collectors.toSet());
	}

	/** Conversions for the scale other than the one already present in the fixture. */
	private static Set<OWLNamedIndividual> newConversions(OWLOntology ontology, String inputScaleIRI) {
		return conversionsWithInputScale(ontology, inputScaleIRI).stream()
				.filter(ind -> !ind.getIRI().equals(IRI.create(OQUO + "scaleConversion-existing")))
				.collect(Collectors.toSet());
	}

	private static Map<IRI, OWLNamedIndividual> mappingsByOutputCategory(OWLOntology ontology,
			OWLNamedIndividual conversion) {
		return objectValuesAsIndividuals(ontology, conversion, OWLUtils.HAS_SCALE_POINT_MAPPING_IRI).stream()
				.collect(Collectors.toMap(
						mapping -> single(objectValues(ontology, mapping, OWLUtils.HAS_OUTPUT_SCALE_CATEGORY_IRI)),
						Function.identity()));
	}
}
