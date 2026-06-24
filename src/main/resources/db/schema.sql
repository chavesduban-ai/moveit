
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `movelt_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `movelt_db`;

DROP TABLE IF EXISTS `calificaciones`;
DROP TABLE IF EXISTS `pedidos`;
DROP TABLE IF EXISTS `usuarios`;

CREATE TABLE `usuarios` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `usuario` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `telefono` VARCHAR(20) NOT NULL DEFAULT '',
  `clave` VARCHAR(255) NOT NULL,
  `rol` ENUM('administrador','repartidor','cliente') NOT NULL DEFAULT 'cliente',
  `ciudad` VARCHAR(255) DEFAULT '',
  `direccion` VARCHAR(255) DEFAULT '',
  `estado` ENUM('activo','inactivo') DEFAULT 'activo',
  `fecha_registro` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario` (`usuario`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_rol` (`rol`),
  KEY `idx_estado` (`estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `pedidos` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` INT(11) NOT NULL,
  `recogida` VARCHAR(255) NOT NULL,
  `entrega` VARCHAR(255) NOT NULL,
  `destinatario` VARCHAR(100) NOT NULL,
  `telefono` VARCHAR(30) NOT NULL,
  `descripcion` TEXT NOT NULL,
  `peso` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `servicio` ENUM('express','standard','economy') NOT NULL DEFAULT 'standard',
  `observaciones` TEXT,
  `fecha` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `estado` ENUM('pendiente','aceptado','en_curso','entregado','cancelado') NOT NULL DEFAULT 'pendiente',
  `repartidor` INT(11) DEFAULT NULL,
  `calificacion` DECIMAL(2,1) DEFAULT 0.0,
  PRIMARY KEY (`id`),
  KEY `idx_usuario_id` (`usuario_id`),
  KEY `idx_estado` (`estado`),
  KEY `idx_repartidor` (`repartidor`),
  KEY `idx_fecha` (`fecha`),
  CONSTRAINT `fk_pedido_usuario` FOREIGN KEY (`usuario_id`)
    REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pedido_repartidor` FOREIGN KEY (`repartidor`)
    REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `calificaciones` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `pedido_id` INT(11) NOT NULL,
  `repartidor_id` INT(11) NOT NULL,
  `cliente_id` INT(11) NOT NULL,
  `calificacion` TINYINT UNSIGNED NOT NULL CHECK (`calificacion` BETWEEN 1 AND 5),
  `comentario` TEXT DEFAULT NULL,
  `fecha` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pedido_cliente` (`pedido_id`, `cliente_id`),
  KEY `idx_repartidor` (`repartidor_id`),
  CONSTRAINT `fk_calif_pedido` FOREIGN KEY (`pedido_id`)
    REFERENCES `pedidos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_calif_repartidor` FOREIGN KEY (`repartidor_id`)
    REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_calif_cliente` FOREIGN KEY (`cliente_id`)
    REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `usuarios` (`id`, `usuario`, `email`, `telefono`, `clave`, `rol`, `ciudad`, `direccion`, `estado`) VALUES
(31, 'dylan',     'dylan12baron@gmail.com', '3100000000', '$2y$10$eHVDOS0JAVgRb9neGEDEA.X0Wv2LU0e0ubOsUKuZ5W77BAo4Pql.K', 'administrador', 'Bogotá', '', 'activo'),
(32, 'andres',    'andres@gmail.com',       '3100000001', '$2y$10$McpJUyqNvvguwbMytmG1uOqiq9CTFTPMczclsJBF0gmhk8Gpp.ZKu', 'repartidor',     'Bogotá', '', 'activo'),
(33, 'duban',     'duban@gmail.com',        '3100000002', '$2y$10$HTmw91MXam2hSl8.Q7tQHudoDluUOJ3MTXH9r3KYSAvoaiy3qvtCK', 'cliente',        'Bogotá', '', 'activo'),
(34, 'jaramillo', 'jara@gmail.com',         '3100000003', '$2y$10$fWsNt7EUdI1wr0/h3p8JK.FxpQoS6OdvDnoAp1fOUMkmDa2elikUe', 'cliente',        'Bogotá', '', 'activo'),
(35, 'rengifo',   'ren@gmail.com',          '3100000004', '$2y$10$9/IZdcex6mLUeV3Hq59DQ.3wvAslUXJ0p6x9uIVl6wQYfPZmv811m', 'cliente',        'Bogotá', '', 'activo'),
(36, 'mario',     'mar@gmail.com',          '3100000005', '$2y$10$pt/ypyh8Ac1GxwX6cP84MeoG.NlWVa4OjqRqfqOVd0ORhiZPboNea', 'cliente',        'Bogotá', '', 'activo'),
(38, 'kevin',     'kevin@gmail.com',        '3100000006', '$2y$10$vpbp5W4jryrjFT/9ZIyND.z1oqJ9ih.lzi90JzbAyfJG0xruVzarq', 'repartidor',     'Bogotá', '', 'activo');

INSERT INTO `pedidos` (`id`, `usuario_id`, `recogida`, `entrega`, `destinatario`, `telefono`, `descripcion`, `peso`, `servicio`, `observaciones`, `fecha`, `estado`, `repartidor`, `calificacion`) VALUES
(27, 33, 'calle 35a #9-4',   'cra 26 #7-40',            'andres',       '310581338',  'duro',                 435.00, 'standard', '',       '2025-12-09 10:06:47', 'entregado', 32, 0.0),
(28, 33, 'cra 54 # 34-40',   'calle 39 c sur # 2 R 17', 'duban',        '3022224760', 'fragil',               9.20,   'economy',  '',       '2025-12-09 13:29:00', 'entregado', 32, 0.0),
(29, 33, 'calle 35a #9-4',   'cra 26 #7-40',            'andres',       '3102248657', 'grande',               10.00,  'express',  '',       '2025-12-09 13:29:39', 'entregado', 32, 0.0),
(30, 33, 'calle 35a #9-7',   'calle 39 c sur # 2 R 17', 'julio',        '3118440964', 'mediano',              21.00,  'express',  '',       '2025-12-09 13:30:50', 'entregado', 32, 0.0),
(31, 33, 'calle 35a #7-40',  'cra 26 #7-40',            'andres vela',  '3213478124', 'mediano',              11.00,  'standard', '',       '2025-12-09 13:31:23', 'entregado', 32, 0.0),
(32, 33, 'calle 35a #9-5',   'cra 26 #7-40',            'andres',       '23456789',   'duro',                 213.00, 'express',  'ewrrht', '2025-12-09 14:04:47', 'aceptado',  32, 0.0),
(33, 36, 'calle 35a #9-5',   'cra 26 #7-40',            'andres',       '23456789',   'duro',                 213.00, 'economy',  '',       '2025-12-09 14:36:46', 'entregado', 38, 0.0),
(34, 33, 'carrera 5 # 7-45', 'calle 45 #67-56',         'duban',        '3105813385', 'caja con herramienta', 50.00,  'express',  '',       '2025-12-09 14:59:48', 'aceptado',  38, 0.0);

ALTER TABLE `usuarios` AUTO_INCREMENT = 39;
ALTER TABLE `pedidos` AUTO_INCREMENT = 35;

COMMIT;
