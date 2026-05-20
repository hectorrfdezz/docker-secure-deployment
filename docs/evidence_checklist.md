# Evidence Checklist

Esta lista sirve para comprobar que el repositorio contiene evidencias suficientes para la entrega.

## Capturas recomendadas

Guardar las capturas en:

```text
docs/images/
```

| Archivo recomendado | Qué debe mostrar |
|---|---|
| `git-flow.png` | Resultado de `git log --graph --oneline --decorate --all`. |
| `docker-ps.png` | Resultado de `docker compose ps` con servicios levantados. |
| `https-localhost.png` | Navegador accediendo a `https://localhost`. |
| `admin-basic-auth.png` | Petición de usuario/contraseña al entrar en `/admin`. |
| `sftp-upload.png` | Subida de un archivo mediante SFTP o cliente SFTP. |
| `docker-stats.png` | Resultado de `docker stats` durante recargas o prueba de carga. |
| `backend-bypass-blocked.png` | Comando `curl http://localhost:8080/...` fallando. |

## Comandos útiles para las evidencias

```bash
docker compose ps
```

```bash
docker stats
```

```bash
curl -I http://localhost
```

```bash
curl -k https://localhost/api/message
```

```bash
curl http://localhost:8080/api/message
```

```bash
git log --graph --oneline --decorate --all
```

## Comprobación final antes de entregar

- [ ] El README explica la arquitectura.
- [ ] `deployment_manual.md` explica cómo arrancar y probar el entorno.
- [ ] `administration_manual.md` documenta hardening, recursos y logs.
- [ ] `critical_thinking.md` responde a los 4 retos de seguridad.
- [ ] `git_flow_report.md` incluye o referencia la captura del historial.
- [ ] `docs/images/git-flow.png` existe.
- [ ] Nginx es el único punto de entrada web.
- [ ] El backend no publica el puerto `8080`.
- [ ] SFTP funciona sin `chmod 777`.
- [ ] Hay evidencia de `docker stats` o al menos instrucciones claras para mostrarla.
