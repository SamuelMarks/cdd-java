/* SDK design idea that is transport agnostic */

import Path from "path";
import assert from "assert";

class Response<T> {
    ok: boolean;
    body: <T>() => T;
}

class Pet {
    name: string;
    static equals(pet0: Pet, pet1): boolean { return pet0.name == pet1.name; }
}

class PetSDK {
    constructor(public access_token: string) {}

    @Path("/api/pet/<name>", "GET", ("application/json", "text/xml"))
    getPetByName(name: string): Response<Pet>;

    @Path("/api/pet", "POST", ("application/json", "text/xml"))
    createPet(pet: Pet): Response<Pet>;
}

const pet_sdk: PetSDK<OkHttpClient> = CreateClient<OkHttpClient>(PetSDK.class);

const new_pet_to_make: Pet = {name: "kitten"};

const pet_create_response: Response<Pet> = pet_sdk.createPet(new_pet_to_make);
assert(pet_create_response.ok);
const pet_created: Pet = pet_create_response.body<Pet>(); assert(Pet.equals(new_pet_to_make, pet_created));


const pet_get_response: Response<Pet> = pet_sdk.getPetByName("kitten");
assert(pet_get_response.ok);
const pet_gotten: Pet = pet_get_response.body<Pet>();
assert(Pet.equals(new_pet_to_make, pet_gotten));
