package br.com.arreda.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VeiculoResponseDTO {

   private Long id;
   private String placa;
   private String marca;
   private String modelo;
   private String cor;
   private int capacidadePassageiros;
}
