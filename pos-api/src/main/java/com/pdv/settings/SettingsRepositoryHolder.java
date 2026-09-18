package com.pdv.settings;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de ajustes (interfaz de primer nivel: Spring Data no registra las anidadas). */
public interface SettingsRepositoryHolder extends JpaRepository<Settings, Long> {}
