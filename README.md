## Parcial ARSW BACK

- David Alejandro Patacon Henao
-  cliente concurrente:  https://github.com/AlejandroHenao2572/ConcurrentClient.git
-  cliente react:   https://github.com/AlejandroHenao2572/cliente-react.git
- video: https://youtu.be/ev7E94qFxRc

## Arquitectura 
![alt text](img/image.png)

## Servidor backend:

### Controlador: 
En \src\main\java\com\parcial_back\controller\TurnController.java

```java

@RestController
@RequestMapping("/api/turn")
@CrossOrigin(origins = "*")
public class TurnController {

    private final TurnService turnService;

    public TurnController(TurnService turnService) {
        this.turnService = turnService;
    }

    @GetMapping("/ticket")
    public ResponseEntity<Turn> getTicket(){
        return(ResponseEntity.ok(turnService.getTicket()));
    }

    @GetMapping("/check/ticket")
    public ResponseEntity<Turn> checkTicket(){
        return(ResponseEntity.ok(turnService.checkTicket()));
    }
}
```


Este controllador expone dos endpoints
- `GET/api/turn/ticket` para generar un nuevo ticket. Este se crea con un id numerico y estado created  
Response:  
```json
code: 200
{
  "id": 1,
  "status": "CREATED"
}
```
- `GET/api/check/ticket` para checkear el ticket mas antiguo. Este se queda en estado called  
Response:  
```json
code: 200
{
  "id": 1,
  "status": "CALLED"
}
```

### Servicio
En \src\main\java\com\parcial_back\service\TurnService.java

```java
@Service
public class TurnService {

    private final TurnPersistence turnPersistence;
    private final SimpMessagingTemplate messagingTemplate;

    public TurnService(TurnPersistence turnPersistence, SimpMessagingTemplate messagingTemplate) {
        this.turnPersistence = turnPersistence;
        this.messagingTemplate = messagingTemplate;
    }

    public synchronized Turn getTicket() {
        Turn nuevoTurn = new Turn(generateTurnId(), "CREATED");
        turnPersistence.saveTurn(nuevoTurn.getId(), nuevoTurn);
        return nuevoTurn;
    }

    public synchronized Turn checkTicket() {
        Turn lastTurn = turnPersistence.getLastCreatedTurn();
        if (lastTurn == null) {
            return null;
        }
        if (lastTurn.getStatus().equals("CREATED")) {
            lastTurn.setStatus("CALLED");
            turnPersistence.saveTurn(lastTurn.getId(), lastTurn);

            // PUBLICAR EVENTO RT
            messagingTemplate.convertAndSend("/topic/ticket-called", lastTurn);
        }
        return lastTurn;
    }

    private int generateTurnId() {
        return turnPersistence.getTurnCount() + 1;
    }
}
```

Este servicio de encarga de:

- Logica de negocio
- Crear un nuevo ticket con id y estado
- Marcar el ultimo ticket como llamado
- Usar la capa de persistencia para llamar los tickets
- Publicar el evento en RT con websockets
- Los metodos son sincronizados para en caso de request concurrentes 

### Persistencia
En \src\main\java\com\parcial_back\persistence\TurnPersistence.java

```java
@Repository
public class TurnPersistence {
    private final Map<Integer, Turn> Turns = new ConcurrentHashMap<>();

    public void saveTurn(Integer id, Turn turn){
        Turns.put(id, turn);
    }

    public Turn getLastCreatedTurn(){
        return Turns.values().stream()
                .filter(turn -> turn.getStatus().equals("CREATED"))
                .findFirst()
                .orElse(null);
    }

    public Integer getTurnCount(){
        return Turns.size();
    }
}
```

- Persistencia basica en memoria
- Crea un mapa concurrente para guardas los tickets
- Se encarga de la persistencia guardado y consultas de sobre los tickets

## Config websockets
En \src\main\java\com\parcial_back\persistence\WebSocketConfig.java

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint WebSocket principal
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); 
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // prefijos para los destinos de mensajes
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```


- Define el endpoint websocktet `/ws`
- Establece el prefijo para el envio de mensajes RT

## Modelo
En \src\main\java\com\parcial_back\model\Turn.java

```java
public class Turn {

    private int id;
    private String status;

    public Turn(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

- Clase de dominio basica para representar un ticket con id y estado(CREATED O CALLED)

