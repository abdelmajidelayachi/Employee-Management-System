package io.hahnsoftware.emp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;

public class SwaggerConfig {

    public static OpenAPI createOpenAPI() {
        Components components = new Components()
                .addSecuritySchemes("basicAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic"))
                .addSchemas("Employee", createEmployeeSchema())
                .addSchemas("Error", createErrorSchema());

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Employee Management System API")
                        .version("1.0.0")
                        .description("REST API for managing employee records")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"))
                .components(components);

        // Add paths
        addEmployeePaths(openAPI);

        return openAPI;
    }

    private static Schema<?> createEmployeeSchema() {
        return new Schema<>()
                .type("object")
                .addProperties("id", new Schema<>().type("integer").format("int64"))
                .addProperties("employeeId", new Schema<>().type("string"))
                .addProperties("fullName", new Schema<>().type("string"))
                .addProperties("email", new Schema<>().type("string").format("email"))
                .addProperties("jobTitle", new Schema<>().type("string"))
                .addProperties("department", new Schema<>().type("string"))
                .addProperties("hireDate", new Schema<>().type("string").format("date"))
                .required(List.of("employeeId", "fullName", "email"));
    }

    private static Schema<?> createErrorSchema() {
        return new Schema<>()
                .type("object")
                .addProperties("code", new Schema<>().type("integer"))
                .addProperties("message", new Schema<>().type("string"));
    }

    private static void addEmployeePaths(OpenAPI openAPI) {
        PathItem employeesPath = new PathItem();

        // GET /api/employees
        employeesPath.get(new io.swagger.v3.oas.models.Operation()
                .summary("Get all employees")
                .description("Returns a list of all employees")
                .tags(List.of("Employees"))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("List of employees")
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(new ArraySchema()
                                                        .items(new Schema<>().$ref("#/components/schemas/Employee"))))))));

        // POST /api/employees
        employeesPath.post(new io.swagger.v3.oas.models.Operation()
                .summary("Create a new employee")
                .description("Creates a new employee record")
                .tags(List.of("Employees"))
                .requestBody(new io.swagger.v3.oas.models.parameters.RequestBody()
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/Employee")))))
                .responses(new ApiResponses()
                        .addApiResponse("201", new ApiResponse().description("Employee created"))
                        .addApiResponse("400", new ApiResponse()
                                .description("Invalid input")
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/Error")))))));

        openAPI.path("/api/employees", employeesPath);

        // Individual employee operations
        PathItem employeeByIdPath = new PathItem();

        // GET /api/employees/{id}
        employeeByIdPath.get(new io.swagger.v3.oas.models.Operation()
                .summary("Get employee by ID")
                .tags(List.of("Employees"))
                .addParametersItem(new Parameter()
                        .name("id")
                        .in("path")
                        .required(true)
                        .schema(new Schema<>().type("integer").format("int64")))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("Employee found")
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/Employee")))))
                        .addApiResponse("404", new ApiResponse().description("Employee not found"))));

        openAPI.path("/api/employees/{id}", employeeByIdPath);
    }
}