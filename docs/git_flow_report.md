# Git Flow Report

Este documento resume el flujo Git utilizado en el proyecto y la evidencia que debe acompañar a la entrega.

## 1. Ramas utilizadas

El proyecto se organiza siguiendo una estructura Git Flow:

| Rama | Uso |
|---|---|
| `main` | Versión final estable para entregar. |
| `develop` | Rama de integración durante el desarrollo. |
| `feature/*` | Ramas para nuevas funcionalidades o mejoras. |
| `release/*` | Rama de preparación antes de cerrar una versión. |
| `hotfix/*` | Rama para corregir incidencias urgentes desde `main`. |

## 2. Ramas relevantes del proyecto

Ramas usadas o esperadas en este proyecto:

```text
main
develop
feature/frontend
feature/backend
feature/nginx-hardening
feature/sftp
feature/documentacion
release/1.0.0
hotfix/certificados
```

No es obligatorio que los nombres sean exactamente esos, pero sí debe verse una separación clara entre desarrollo, release y hotfix.

## 3. Flujo seguido

1. Desarrollo inicial en `develop`.
2. Creación de ramas `feature/` para componentes concretos.
3. Integración de las features en `develop`.
4. Creación de `release/1.0.0` para preparar la entrega.
5. Simulación del incidente de `server.key` y corrección en `hotfix/certificados`.
6. Merge del hotfix tanto a `main` como a `develop`.
7. Merge final de release a `main`.

## 4. Comandos de comprobación

Para generar la evidencia del historial:

```bash
git log --graph --oneline --decorate --all
```

También se puede comprobar el listado de ramas:

```bash
git branch -a
```

## 5. Captura obligatoria

La captura debe mostrar el resultado de:

```bash
git log --graph --oneline --decorate --all
```

Guardar la imagen en:

```text
docs/images/git-flow.png
```

Incluirla aquí:

![Git Flow](images/git-flow.png)

## 6. Qué debe verse en la captura

La captura debería mostrar:

- `main`.
- `develop`.
- Al menos una rama `feature/`.
- Una rama `release/`.
- Una rama `hotfix/`.
- Commits con mensajes descriptivos.
- Merges visibles entre ramas.

## 7. Ejemplos de mensajes correctos

```text
feat: crear arquitectura base con docker compose
feat: añadir proxy nginx con tls y basic auth
feat: configurar sftp con volumen compartido
fix: ajustar permisos uid gid entre sftp y nginx
docs: actualizar manuales y evidencias del despliegue
hotfix: regenerar certificados y documentar incidente
chore: preparar release 1.0.0
```

## 8. Valoración

El flujo Git demuestra que el proyecto no se ha construido con commits sueltos directamente sobre `main`, sino siguiendo una metodología de desarrollo controlada. El hotfix de certificados representa la respuesta al incidente de seguridad simulado.
