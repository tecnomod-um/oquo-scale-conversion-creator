package es.um.dis.formatOquo.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OWLUtils {
	public static final String OQUO_NS = "https://purl.org/oquo#";
	public static final String OM_NS = "http://www.wurvoc.org/vocabularies/om-1.8/";
	public static final String QM_NS = "http://purl.org/net/QualityModel#";
	public static final String IAO_NS = "http://purl.obolibrary.org/obo/";
	public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
	public static final String IAO_DEFINITION_IRI = IAO_NS + "IAO_0000115";
	public static final String RAW_SCALE_IRI = OQUO_NS + "RawScale";
	public static final String OQUARE_SCALE_IRI = OQUO_NS + "OquareScale";
	public static final String OQUARE_FIVE_CATEGORIES_SCALE_IRI = OQUO_NS + "OquareFiveCategoriesScale";
	public static final String HAS_RANKING_FUNCTION_IRI = QM_NS + "hasRankigFunction";
	public static final String HAS_SCALE_IRI = QM_NS + "hasScale";
	public static final String HAS_INPUT_SCALE_IRI = OQUO_NS + "hasInputScale";
	public static final String HAS_OUTPUT_SCALE_IRI = OQUO_NS + "hasOutputScale";
	public static final String HAS_SCALE_POINT_MAPPING_IRI = OQUO_NS + "hasScalePointMapping";
	public static final String HAS_INPUT_CARDINAL_INTERVAL_IRI = OQUO_NS + "hasInputCardinalInterval";
	public static final String HAS_INPUT_SCALE_CATEGORY_IRI = OQUO_NS + "hasInputScaleCategory";
	public static final String HAS_OUTPUT_SCALE_CATEGORY_IRI = OQUO_NS + "hasOutputScaleCategory";
	public static final String HAS_INTERVAL_LOWER_VALUE_IRI = OQUO_NS + "hasIntervalLowerValue";
	public static final String HAS_INTERVAL_UPPER_VALUE_IRI = OQUO_NS + "hasIntervalUpperValue";
	public static final String HAS_INTERVAL_LOWER_BOUND_TYPE_IRI = OQUO_NS + "hasIntervalLowerBoundType";
	public static final String HAS_INTERVAL_UPPER_BOUND_TYPE_IRI = OQUO_NS + "hasIntervalUpperBoundType";
	public static final String INTERVAL_IRI = OQUO_NS + "Interval";
	public static final String OPEN_BOUND_TYPE_IRI = OQUO_NS + "OpenIntervalBound";
	public static final String CLOSED_BOUND_TYPE_IRI = OQUO_NS + "ClosedIntervalBound";
	public static final String CARDINAL_TO_ORDINAL_SCALE_POINT_MAPPING_IRI = OQUO_NS
			+ "CardinalToOrdinalScalePointMapping";
	public static final String ORDINAL_TO_ORDINAL_SCALE_POINT_MAPPING_IRI = OQUO_NS
			+ "OrdinalToOrdinalScalePointMapping";
	public static final String DECIMAL_IRI = XSD_NS + "decimal";
	public static final String DOUBLE_IRI = XSD_NS + "double";

	public static void addAnnotation(OWLOntology ontology, OWLEntity entity, IRI annotationPropertyIRI,
			String annotationValue) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty annotationProperty = df.getOWLAnnotationProperty(annotationPropertyIRI);
		OWLLiteral annotationValueLiteral = df.getOWLLiteral(annotationValue);
		OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(annotationProperty, entity.getIRI(), annotationValueLiteral);
		ontology.addAxiom(axiom);
	}

	public static void addValuesAxiom(OWLOntology ontology, OWLClassExpression owlClass, IRI objectPropertyIRI,
			OWLNamedIndividual individual) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLObjectProperty objectProperty = df.getOWLObjectProperty(objectPropertyIRI);
		OWLClassExpression classExpression = df.getOWLObjectHasValue(objectProperty, individual);
		OWLAxiom owlAxiom = df.getOWLSubClassOfAxiom(owlClass, classExpression);
		ontology.add(owlAxiom);
	}

	public static void addIndividualRelation(OWLOntology ontology, OWLNamedIndividual subject, IRI objectPropertyIRI,
			OWLNamedIndividual object) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLObjectProperty objectProperty = df.getOWLObjectProperty(objectPropertyIRI);
		OWLAxiom owlAxiom = df.getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object);
		ontology.add(owlAxiom);
	}

	public static void addIndividualDataProperty(OWLOntology ontology, OWLNamedIndividual subject, IRI dataPropertyIRI,
			double value) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLDataProperty dataProperty = df.getOWLDataProperty(dataPropertyIRI);
		String valueString = "";
		if (value == Double.POSITIVE_INFINITY) {
			valueString = "INF";
		} else if (value == Double.NEGATIVE_INFINITY) {
			valueString = "-INF";
		} else {
			valueString = String.valueOf(value);
		}
		OWLLiteral literal = df.getOWLLiteral(valueString, df.getOWLDatatype(DOUBLE_IRI));
		OWLAxiom owlAxiom = df.getOWLDataPropertyAssertionAxiom(dataProperty, subject, literal);
		ontology.add(owlAxiom);
	}

	public static void addIndividualDataProperty(OWLOntology ontology, OWLNamedIndividual subject, IRI dataPropertyIRI,
			int value) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLDataProperty dataProperty = df.getOWLDataProperty(dataPropertyIRI);
		OWLAxiom owlAxiom = df.getOWLDataPropertyAssertionAxiom(dataProperty, subject, value);
		ontology.add(owlAxiom);
	}

	public static void addDataPropertyRange(OWLOntology ontology, OWLDataProperty property, OWL2Datatype range) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = df.getOWLDataPropertyRangeAxiom(property, range);
		ontology.add(axiom);
	}

	public static void addAnnotation(OWLOntology ontology, OWLEntity entity, IRI annotationPropertyIRI,
			String annotationValue, String lang) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		// OWLAnnotationProperty annotationProperty =
		// df.getOWLAnnotationProperty(annotationPropertyIRI);
		OWLLiteral annotationValueLiteral = null;
		if (lang != null) {
			annotationValueLiteral = df.getOWLLiteral(annotationValue, lang);
		} else {
			annotationValueLiteral = df.getOWLLiteral(annotationValue);
		}
		// OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(annotationProperty,
		// entity.getIRI(), annotationValueLiteral);
		// ontology.addAxiom(axiom);
		addAnnotation(ontology, entity, annotationPropertyIRI, annotationValueLiteral);
	}

	public static void addAnnotationWithComment(OWLOntology ontology, OWLEntity entity, IRI annotationPropertyIRI,
			String annotationValue, String comment) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty annotationProperty = df.getOWLAnnotationProperty(annotationPropertyIRI);
		OWLAnnotationProperty rdfsComment = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT);
		OWLAnnotation commentAnnotation = df.getOWLAnnotation(rdfsComment, df.getOWLLiteral(comment));
		OWLAnnotation annotation = df.getOWLAnnotation(annotationProperty, df.getOWLLiteral(annotationValue));

		OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(entity.getIRI(), annotation,
				Arrays.asList(commentAnnotation));
		ontology.add(axiom);
	}

	public static void addAnnotation(OWLOntology ontology, OWLEntity entity, IRI annotationPropertyIRI,
			OWLAnnotationValue annotationValue) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty annotationProperty = df.getOWLAnnotationProperty(annotationPropertyIRI);
		OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(annotationProperty, entity.getIRI(), annotationValue);
		ontology.add(axiom);
	}

	public static void addAnnotation(OWLOntology ontology, OWLAxiom owlAxiom, IRI annotationPropertyIRI,
			OWLAnnotationValue annotationValue) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty annotationProperty = df.getOWLAnnotationProperty(annotationPropertyIRI);
		OWLAnnotation annotation = df.getOWLAnnotation(annotationProperty, annotationValue);
		Set<OWLAnnotation> newAnnotations = owlAxiom.annotations().collect(Collectors.toSet());
		newAnnotations.add(annotation);

		OWLAxiom annotatedAxiom = owlAxiom.getAnnotatedAxiom(newAnnotations);
		ontology.add(annotatedAxiom);
		ontology.remove(owlAxiom);
	}

	public static void addAnnotation(OWLOntology ontology, OWLAxiom owlAxiom, IRI annotationPropertyIRI,
			String literal) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLLiteral annotationValue = df.getOWLLiteral(literal);
		addAnnotation(ontology, owlAxiom, annotationPropertyIRI, annotationValue);
	}

	public static OWLAxiom addSubclassOf(OWLOntology ontology, OWLClass entity, OWLClassExpression parentEntity) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = df.getOWLSubClassOfAxiom(entity, parentEntity);
		ontology.add(axiom);
		return axiom;
	}

	public static void addSubPropertyOf(OWLOntology ontology, OWLProperty property, OWLProperty parentProperty) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = null;
		if (property.isObjectPropertyExpression() && parentProperty.isObjectPropertyExpression()) {
			axiom = df.getOWLSubObjectPropertyOfAxiom(property.asObjectPropertyExpression(),
					parentProperty.asObjectPropertyExpression());
		}

		if (property.isDataPropertyExpression() && parentProperty.isDataPropertyExpression()) {
			axiom = df.getOWLSubDataPropertyOfAxiom(property.asDataPropertyExpression(),
					parentProperty.asDataPropertyExpression());
		}

		if (property.isOWLAnnotationProperty() && parentProperty.isOWLAnnotationProperty()) {
			axiom = df.getOWLSubAnnotationPropertyOfAxiom(property.asOWLAnnotationProperty(),
					parentProperty.asOWLAnnotationProperty());
		}

		if (axiom != null) {
			ontology.add(axiom);
		} else {
			System.out.println("addSubPropertyOf - incompatible properties");
		}
	}

	public static void addEquivalentClass(OWLOntology ontology, OWLClass entity, OWLClassExpression equivalentEntity) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = df.getOWLEquivalentClassesAxiom(entity, equivalentEntity);
		ontology.add(axiom);
	}

	public static void addEquivalentObjectProperties(OWLOntology ontology, OWLObjectPropertyExpression property1,
			OWLObjectPropertyExpression property2) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = df.getOWLEquivalentObjectPropertiesAxiom(property1, property2);
		ontology.add(axiom);
	}

	public static void addEquivalentDataProperties(OWLOntology ontology, OWLDataPropertyExpression property1,
			OWLDataPropertyExpression property2) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = df.getOWLEquivalentDataPropertiesAxiom(property1, property2);
		ontology.add(axiom);
	}

	public static void addDomain(OWLOntology ontology, OWLProperty property, OWLClassExpression classExpression) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = null;
		if (property.isOWLObjectProperty()) {
			axiom = df.getOWLObjectPropertyDomainAxiom((OWLObjectProperty) property, classExpression);
		} else if (property.isOWLDataProperty()) {
			axiom = df.getOWLDataPropertyDomainAxiom((OWLDataProperty) property, classExpression);
		}
		if (axiom != null) {
			ontology.add(axiom);
		}
	}

	public static void addRange(OWLOntology ontology, OWLObjectProperty objectProperty,
			OWLClassExpression classExpression) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = df.getOWLObjectPropertyRangeAxiom(objectProperty, classExpression);
		ontology.add(axiom);
	}

	public static OWLClass createClass(OWLOntology ontology, IRI classIRI) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLClass owlClass = df.getOWLClass(classIRI);
		OWLAxiom axiom = df.getOWLDeclarationAxiom(owlClass);
		ontology.add(axiom);
		return owlClass;
	}

	public static OWLObjectProperty createObjectProperty(OWLOntology ontology, IRI propertyIRI) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLObjectProperty owlObjectProperty = df.getOWLObjectProperty(propertyIRI);
		OWLAxiom axiom = df.getOWLDeclarationAxiom(owlObjectProperty);
		ontology.add(axiom);
		return owlObjectProperty;
	}

	public static OWLAnnotationProperty createAnnotationProperty(OWLOntology ontology, IRI propertyIRI) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationProperty annotationProperty = df.getOWLAnnotationProperty(propertyIRI);
		OWLAxiom axiom = df.getOWLDeclarationAxiom(annotationProperty);
		ontology.add(axiom);
		return annotationProperty;
	}

	public static OWLNamedIndividual createIndividual(OWLOntology ontology, IRI individualIRI, OWLClass type) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLNamedIndividual individual = df.getOWLNamedIndividual(individualIRI);
		if (type != null) {
			OWLAxiom axiom = df.getOWLClassAssertionAxiom(type, individual);
			ontology.add(axiom);
		}
		OWLAxiom axiom = df.getOWLDeclarationAxiom(individual);
		ontology.add(axiom);
		return individual;
	}

	public static OWLDataProperty createDataProperty(OWLOntology ontology, IRI propertyIRI) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLDataProperty owlDataProperty = df.getOWLDataProperty(propertyIRI);
		OWLAxiom axiom = df.getOWLDeclarationAxiom(owlDataProperty);
		ontology.add(axiom);
		return owlDataProperty;
	}

	public static void addClassAssertion(OWLOntology ontology, IRI individualIRI, IRI owlClassIRI) {
		OWLNamedIndividual individual = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLNamedIndividual(individualIRI);
		addClassAssertion(ontology, individual, owlClassIRI);
	}

	public static void addClassAssertion(OWLOntology ontology, OWLNamedIndividual individual, IRI owlClassIRI) {
		OWLClass owlClass = createClass(ontology, owlClassIRI);
		OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(owlClass,
				individual);
		ontology.add(axiom);
	}

	public static OWLAxiom addObjectSomeValuesFromRestriction(OWLOntology ontology, OWLProperty property,
			OWLClass owlClass, OWLClassExpression classExpression) {
		OWLClassExpression someValuesFromExpression = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLObjectSomeValuesFrom((OWLObjectPropertyExpression) property, classExpression);
		return addSubclassOf(ontology, owlClass, someValuesFromExpression);
	}

	public static OWLAxiom addDataSomeValuesFromRestriction(OWLOntology ontology, OWLDataProperty property,
			OWLClass owlClass, OWLDataRange datatype) {
		OWLClassExpression someValuesFromExpression = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLDataSomeValuesFrom(property, datatype);
		OWLAxiom axiom = addSubclassOf(ontology, owlClass, someValuesFromExpression);
		return axiom;

	}

	public static OWLAxiom addObjectAllValuesFromRestriction(OWLOntology ontology, OWLObjectProperty property,
			OWLClass owlClass, OWLClassExpression classExpression) {
		OWLClassExpression allValuesFromExpression = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLObjectAllValuesFrom(property, classExpression);
		OWLAxiom axiom = addSubclassOf(ontology, owlClass, allValuesFromExpression);
		return axiom;
	}

	public static List<OWLNamedIndividual> getIndividualsFromList(OWLOntology ontology, String prefixIRI,
			List<String> listOfNames, OWLClass type) {
		List<OWLNamedIndividual> result = new ArrayList<>();
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		for (String name : listOfNames) {
			OWLNamedIndividual individual = df.getOWLNamedIndividual(prefixIRI + name);
			OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(type,
					individual);
			ontology.add(axiom);
			result.add(individual);
		}
		return result;
	}

	public static void addOneOfAxiom(OWLOntology ontology, OWLClass owlClass, List<OWLNamedIndividual> individuals) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLObjectOneOf oneOf = df.getOWLObjectOneOf(individuals);
		OWLAxiom equivalentAxiom = df.getOWLEquivalentClassesAxiom(owlClass, oneOf);
		ontology.add(equivalentAxiom);
	}

	public static void setDisjointClasses(OWLOntology ontology, Collection<OWLClass> classes) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom = df.getOWLDisjointClassesAxiom(classes);
		ontology.add(axiom);
	}

	public static boolean containsAnnotation(OWLOntology ontology, OWLEntity entity,
			OWLAnnotationProperty annotationProperty) {

		return EntitySearcher.getAnnotationObjects(entity, ontology).anyMatch(annotation -> {
			return annotation.getProperty().equals(annotationProperty);
		});
	}

	public static boolean containsAnnotation(OWLOntology ontology, OWLEntity entity, IRI annotationPropertyIRI) {

		return EntitySearcher.getAnnotationObjects(entity, ontology).anyMatch(annotation -> {
			return annotation.getProperty().getIRI().equals(annotationPropertyIRI);
		});
	}

	public static OWLClassExpression getOrClassExpression(OWLOntology ontology, Collection<OWLClass> owlClasses) {
		return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectUnionOf(owlClasses);
	}

	public static boolean hasValue(OWLOntology ontology, OWLNamedIndividual subject, OWLObjectProperty property,
			OWLIndividual value) {
		return ontology.objectPropertyAssertionAxioms(subject)
				.anyMatch(ax -> ax.getProperty().equals(property) && ax.getObject().equals(value));
	}

}
