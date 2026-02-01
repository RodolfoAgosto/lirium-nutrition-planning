# Lirium – Nutrition Planning Platform (MVP)

## 🎯 System Purpose

Lirium es una plataforma backend que permite a los nutricionistas diseñar planes de nutrición personalizados y a los pacientes seguir planes diarios viables, basados en objetivos nutricionales calculados automáticamente por el sistema.

El sistema transforma reglas profesionales en planes concretos y accionables, manteniendo al nutricionista como responsable del criterio clínico.

---

## 👥 Actors

- **Nutricionista**
- **Paciente**

---

## 🧩 Problem Statement

La planificación nutricional suele apoyarse en procesos manuales o herramientas genéricas, lo que genera fragmentación de la información, duplicación de cálculos y dificultades en la gestión y el seguimiento de planes alimentarios.

Lirium centraliza la definición, el cálculo y la visualización de planes nutricionales personalizados.

---

## 📐 Scope (MVP)

### Nutricionista
- Gestionar pacientes (crear, modificar, consultar, desactivar)
- Crear y gestionar planes de nutrición
- Definir objetivos nutricionales
- Realizar seguimiento básico del paciente

### Paciente
- Registrarse y acceder a la plataforma
- Visualizar su plan de nutrición
- Visualizar objetivos nutricionales
- Registrar datos básicos (peso, adherencia, observaciones)

---

## 🔁 Main Functional Flow

1. El nutricionista define **plantillas de planes nutricionales**, compuestas por:
   - reglas nutricionales estructuradas (utilizadas por el sistema)
   - guías alimentarias descriptivas (orientadas al paciente)

2. El nutricionista registra los datos y objetivos del paciente.

3. El sistema:
   - calcula los objetivos nutricionales personalizados
   - genera un plan diario concreto a partir de la plantilla más adecuada

4. El paciente:
   - accede a su plan nutricional
   - visualiza objetivos
   - recibe guías alimentarias simples para llevar el plan a la práctica

---

## 🧠 Domain Concepts

- Nutricionista
- Paciente
- Plan Base de Nutrición
- Plan de Nutrición
- Alimento
- Nutriente
- Objetivo Nutricional

---

## ⚙️ Decisions & Assumptions

- Sin autenticación ni autorización en esta etapa
- Enfoque exclusivo en backend (API REST)
- Valores nutricionales basados en tablas estándar
- El sistema asiste al nutricionista, no reemplaza su criterio profesional
