package com.campito.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/comprascredito")
@Tag(name = "ComprasCredito", description = "Operaciones para la gestión de compras con crédito")
public class ComprasCreditoController {

}
