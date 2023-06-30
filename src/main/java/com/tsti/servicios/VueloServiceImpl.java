package com.tsti.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsti.dao.CiudadDAO;
import com.tsti.dao.VueloDAO;
import com.tsti.dto.VueloDTO;
import com.tsti.entidades.Vuelo;
import com.tsti.entidades.Vuelo.EstadoVuelo;
import com.tsti.entidades.Vuelo.TipoVuelo;
import com.tsti.entidades.Ciudad;
import com.tsti.faker.CiudadFactory;
import com.tsti.faker.GenerarPrecioNeto;
import com.tsti.presentacion.CrearVueloForm;
import com.tsti.presentacion.EditarVueloForm;
/**
 * @author Bruno
 * 
 * */
@Service
public class VueloServiceImpl implements IVueloService{
	
	private VueloDAO vueloDAO;	
	private CiudadDAO ciudadDAO;
	private CiudadFactory ciudadFactory;
	private Optional <Vuelo> vueloOptional;
	@Autowired
	public VueloServiceImpl(VueloDAO vueloDAO, CiudadDAO ciudadDAO, CiudadFactory ciudadFactory, Optional <Vuelo> vueloOptional) {
		this.vueloDAO = vueloDAO;	
		this.ciudadDAO = ciudadDAO;
		this.ciudadFactory = ciudadFactory;
		this.vueloOptional= vueloOptional;
	}
	@Override
	public VueloDTO crearVuelo(CrearVueloForm vueloForm) {
		
		Vuelo vuelo = new Vuelo();
		VueloDTO vueloDTO; 
		Ciudad origen = new Ciudad();
		Ciudad destino = new Ciudad();
		
		try {
			
			if(ciudadDAO.existsByCodAeropuerto("SAAV")){
				
				origen = ciudadDAO.findFirstByCodAeropuertoAndNombreCiudad
														("SAAV", "Sauce Viejo");
			} else{
				
				origen = ciudadFactory.getCiudadSauceViejo();
			}
			
		}catch(Exception e){
			
		}
		
		
		if(vueloForm.getIdDestino() != null){
			
			try {
				Optional<Ciudad>ciudadOptional = ciudadDAO.findById(vueloForm.getIdDestino());
				
				if(ciudadOptional.isPresent()) {
					
					destino = ciudadOptional.get();
				}
				
			}catch(Exception e){
				
			}
					
			
		}else{
						
			destino.setCodAeropuerto(vueloForm.getCodAeropuerto());
			destino.setNombreCiudad(vueloForm.getNombreCiudad());
			destino.setProvincia(vueloForm.getProvincia());
			destino.setPais(vueloForm.getPais());
			destino.setCodPostal(vueloForm.getCodPostal());
			
		}
		
		try {
			
			ciudadDAO.save(origen);	
			ciudadDAO.save(destino);
		
		}catch(Exception e){
			
		
		}			
				
		vuelo.setAerolinea(vueloForm.getAerolinea());
		vuelo.setAvion(vueloForm.getAvion());
		vuelo.setFechaPartida(vueloForm.getFechaPartida());
		vuelo.setHoraPartida(vueloForm.getHoraPartida());
		vuelo.setNroFilas(vueloForm.getNroFilasAsientos());
		vuelo.setNroColumnas(vueloForm.getNroColumnasAsientos());
		vuelo.setOrigen(origen);
		vuelo.setDestino(destino);
		vuelo.setTipoVuelo();
		vuelo.setPrecioNeto(vueloForm.getPrecioNeto()); 
		
		if(vueloForm.getPrecioNeto() == null) {
			
			if(vuelo.getTipoVuelo() == TipoVuelo.NACIONAL) {
				
			
				vuelo.setPrecioNeto(GenerarPrecioNeto.generarPrecioNetoPesos());
			
			}else {			
				
				vuelo.setPrecioNeto(GenerarPrecioNeto.generarPrecioNetoDolares());
			
			}
			
		}		
				
		vuelo.setEstadoVuelo(EstadoVuelo.REGISTRADO);
		
		try {
			
		vuelo = vueloDAO.save(vuelo);
		
		}catch(Exception e){
			
		
		}
				
		vueloDTO = new VueloDTO(vuelo);
		
		vueloDTO.setNroVuelo(vuelo.getNroVuelo());
		
		return vueloDTO;
				
	}
	
	@Override
	public VueloDTO cancelarVuelo(Long nroVuelo){		
		
		Vuelo vuelo = new Vuelo();
		
		try{
			
			Optional<Vuelo> vueloOptional = vueloDAO.findById(nroVuelo);
			
			if (vueloOptional.isPresent()) {
	            
				vuelo = vueloOptional.get();
				
				vuelo.setEstadoVuelo(EstadoVuelo.CANCELADO);
				vuelo.setPrecioNeto(BigDecimal.valueOf(0.00));
				
				vueloDAO.save(vuelo);
			}
			
		}catch(Exception e){
			
		}
				
		return new VueloDTO(vuelo);
	}


	@Override
	public VueloDTO reprogramarVuelo(Long nroVuelo, EditarVueloForm vueloForm){
		
		Vuelo vuelo = new Vuelo();		
		
		try{
			Optional<Vuelo> vueloOptional = vueloDAO.findById(nroVuelo);
	        
	        if (vueloOptional.isPresent()) {
	            
	        	vuelo = vueloOptional.get();
	                       		
		        vuelo.setFechaPartida(vueloForm.getFechaPartida());
		        vuelo.setHoraPartida(vueloForm.getHoraPartida());
	        
	        // Cambiar estado a reprogramado
	        vuelo.setEstadoVuelo(EstadoVuelo.REPROGRAMADO);
	        vueloDAO.save(vuelo);        
	        
	        }
	        
		}catch(Exception e){
			
			
		}
		           
        return new VueloDTO(vuelo);        
    }
	// 
	@Override
	public Optional<Vuelo> findById(Long nroVuelo){
		
		
		
		try
		{
			return vueloDAO.findById(nroVuelo);
		
		}catch(Exception e){
			
		}
		return vueloDAO.findById(nroVuelo);
	
	}	
	@Override
	public List<VueloDTO> findByDestinoAndFechaPartida(String destino, LocalDate fecha) {
        
		List<Vuelo> vuelos = vueloDAO.findByDestinoAndFechaPartida(destino, fecha);
		List<VueloDTO> vuelosDTO = new ArrayList<>();
		
		for(Vuelo vuelo : vuelos ){			
			
			vuelosDTO.add(new VueloDTO(vuelo));
			
		}
		
		return vuelosDTO;
    }
	
	@Override
	public List<VueloDTO> findByDestino(String destino) {
		
		List<Vuelo> vuelos = vueloDAO.findByDestino(destino);
		List<VueloDTO> vuelosDTO = new ArrayList<>();
		
		for(Vuelo vuelo : vuelos ){			
			
			vuelosDTO.add(new VueloDTO(vuelo));
			
		}
		
		return vuelosDTO;		
				
	}
	
	@Override
	public List<VueloDTO> findByFechaPartida(LocalDate fecha) {
		
		List<Vuelo> vuelos = vueloDAO.findByFechaPartida(fecha);
		List<VueloDTO> vuelosDTO = new ArrayList<>();	
			
		for(Vuelo vuelo : vuelos ){			
			
			vuelosDTO.add(new VueloDTO(vuelo));
			
		}
		
		return vuelosDTO;			
		
    }
	@Override
	public List<Vuelo> obtenerVuelosPorTipo(TipoVuelo tipoVuelo) {
        return vueloDAO.findByTipoVuelo(tipoVuelo);
    }
	@Override
	public List<VueloDTO> getAll() {
		
		
		List<Vuelo> vuelos = vueloDAO.findAll();
		List<VueloDTO> vuelosDTO = new ArrayList<>();
		
		for(Vuelo  vuelo : vuelos ){			
				
			vuelosDTO.add(new VueloDTO(vuelo));
			
		}
		
		return vuelosDTO;			
		
	}
	@Override
	public List<VueloDTO> findAllByEstadoVuelo(EstadoVuelo estadoVuelo){
		
		
		List<Vuelo> vuelosPorEstado = vueloDAO.findAllByEstadoVuelo(estadoVuelo);
		List<VueloDTO> vuelosPorEstadoDTO = new ArrayList<>();	
			
		for(Vuelo  vuelo : vuelosPorEstado ){			
			
			vuelosPorEstadoDTO.add(new VueloDTO(vuelo));
			
		}
		
		return vuelosPorEstadoDTO;
		
	}

}
