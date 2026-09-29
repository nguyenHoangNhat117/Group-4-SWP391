/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

import addObserver from "./animation.js";

const body = document.querySelector("body");
const texts = document.querySelectorAll(".hero-wrapper .text");
const heroImg = document.querySelector("#hero-img");

console.log(texts);

if (body.className !== "disable-animation") {
    texts.forEach(text => {
        text.classList.add("hidden");
    });
    heroImg.classList.add("hidden");
    addObserver();
}


