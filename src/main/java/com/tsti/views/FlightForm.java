package com.tsti.views;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import com.tsti.dao.CiudadDAO;
import com.tsti.entidades.Ciudad;
import com.tsti.entidades.Vuelo;
import com.tsti.i18n.AppI18NProvider;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;


public class FlightForm extends FormLayout{
		
	private static final long serialVersionUID = 5593602849999149819L;
	private final AppI18NProvider i18NProvider;
	private final CiudadDAO ciudadDAO;
	TextField nroVuelo = new TextField("Flight Number");
	TextField aerolinea = new TextField("Airline");	
	DatePicker fechaPartida = new DatePicker("Date");
	LocalDate now = LocalDate.now();
	TimePicker horaPartida = new TimePicker("Hour");	
	ComboBox<Ciudad> destino = new ComboBox<>("Arrival");
	//ComboBox<Ciudad> estadoVuelo = new ComboBox<>("Status");
	BigDecimalField precioNeto = new BigDecimalField();  
	TextField avion = new TextField("Aircraft");
	
	String flightIdLabel;
	String airlineLabel;
	String aircraftLabel;
	String departureLabel;
	String arrivalLabel;
	String typeLabel;
	String dateLabel;
	String timeLabel;
	String priceLabel;
	String statusLabel;
	String seatsLabel;
	String saveButtonLabel;
	String deleteButtonLabel;
	String cancelButtonLabel;
	
	Button save;
	Button delete;
	Button close;

	Binder<Vuelo> binder = new BeanValidationBinder<>(Vuelo.class);
	
	public FlightForm(AppI18NProvider i18nProvider, CiudadDAO ciudadDAO) {
		super();
		this.i18NProvider = i18nProvider;		
		this.ciudadDAO = ciudadDAO;
		
		addClassName("flight-form");
		binder.bindInstanceFields(this);
		
		//Labels
		initializeLabels();	
		//
		configureDateTimePickers(); 
		//Buttons
		save = new Button(saveButtonLabel);
		delete = new Button(deleteButtonLabel);
		close = new Button(cancelButtonLabel);		
		
		add(nroVuelo,
			aerolinea,			
			destino,			
			//estadoVuelo,
			fechaPartida,
			horaPartida,
			precioNeto,
			createButtonsLayout());
	}
	
	private void configureDateTimePickers() {
		this.fechaPartida.setMin(now);
		this.fechaPartida.setMax(now.plusDays(330));
		this.fechaPartida.setInitialPosition(now);
		this.horaPartida.setStep(Duration.ofMinutes(1));
	}
	
	private Component createButtonsLayout() {
		
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		
		save.addClickShortcut(Key.ENTER);
		close.addClickShortcut(Key.ESCAPE);
		
		save.addClickListener(event -> validateAndSave());
		delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
		close.addClickListener(event -> fireEvent(new CloseEvent(this)));
		
		binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
		
		return new HorizontalLayout(save, delete, close);
	}
	
	private void initializeLabels(){
		flightIdLabel = i18NProvider.getTranslation("flight-id", getLocale());
		airlineLabel = i18NProvider.getTranslation("airline", getLocale());		
		departureLabel = i18NProvider.getTranslation("departure", getLocale());
		arrivalLabel = i18NProvider.getTranslation("arrival", getLocale());
		typeLabel = i18NProvider.getTranslation("type", getLocale());
		dateLabel = i18NProvider.getTranslation("departure-date", getLocale());
		timeLabel = i18NProvider.getTranslation("departure-hour", getLocale());
		priceLabel = i18NProvider.getTranslation("price", getLocale());
		statusLabel = i18NProvider.getTranslation("flight-status", getLocale());
		seatsLabel = i18NProvider.getTranslation("seats-number", getLocale());
		saveButtonLabel = i18NProvider.getTranslation("save", getLocale());
		deleteButtonLabel = i18NProvider.getTranslation("delete", getLocale());
		cancelButtonLabel = i18NProvider.getTranslation("cancel", getLocale());
		
		//setear labels
		nroVuelo.setLabel(flightIdLabel);
		nroVuelo.setReadOnly(true);
		aerolinea.setLabel(airlineLabel);		
		destino.setLabel(arrivalLabel);
		fechaPartida.setLabel(dateLabel);
		horaPartida.setLabel(timeLabel);
		precioNeto.setLabel(priceLabel);	
		
		destino.setItems(ciudadDAO.findAll());
		destino.setItemLabelGenerator(ciudad -> ciudad.getNombreCiudad() 
										+ ", " + ciudad.getPais());
		destino.addValueChangeListener(event -> {
		    Vuelo vuelo = binder.getBean(); // Obtener el objeto Vuelo vinculado al formulario
		    if(vuelo != null) {
		    	vuelo.setDestino(event.getValue());// Actualizar la propiedad destino con la ciudad seleccionada 
		    }
		    
		});
	}

	private void validateAndSave() {
		if(binder.isValid()) {
			fireEvent(new SaveEvent(this, binder.getBean()));
		}		
	}
	
	public void setFlight(Vuelo vuelo) {
		binder.setBean(vuelo);		
	}
	
	//Events & Listeners
	public static abstract class FlightFormEvent extends ComponentEvent<FlightForm>{
		
		private static final long serialVersionUID = 337235857761915025L;
		
		private Vuelo vuelo;
		
		public FlightFormEvent(FlightForm source, Vuelo vuelo) {
			super(source, false);
			this.vuelo = vuelo;
		}
		
		public Vuelo getVuelo() {
			return vuelo;			
		}
		
	}
	
	public static class SaveEvent extends FlightFormEvent {

		private static final long serialVersionUID = 7699673655922195575L;

		public SaveEvent(FlightForm source, Vuelo vuelo) {
			super(source, vuelo);			
		}
		
	}
	
	public static class DeleteEvent extends FlightFormEvent {

		private static final long serialVersionUID = -4936623292912312293L;

		public DeleteEvent(FlightForm source, Vuelo vuelo) {
			super(source, vuelo);			
		}		
	}
	
	public static class CloseEvent extends FlightFormEvent {
		
		private static final long serialVersionUID = 177022814965118984L;

		public CloseEvent(FlightForm source) {
			super(source, null);			
		}		
	}
	
	public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
		return addListener(DeleteEvent.class, listener);		
	}
	
	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);		
	}
	
	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
		return addListener(CloseEvent.class, listener);		
	}
	
//	private static class NumberFieldBigDecimal extends Div {
//				
//		private static final long serialVersionUID = -8869950694414073667L;
//		
//		
//		public NumberFieldBigDecimal() {
//			
//			BigDecimalField bigDecimalField = new BigDecimalField();
//			bigDecimalField.setWidth("240px");
//			
//			add(bigDecimalField);
//			
//		}
//		
//	}
	
	

}
