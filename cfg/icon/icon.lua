local util = require("cfgGen.util")
local icon = {}

---@class icon.icon
local name2index = {
    id = 1,
    eName = 2,
    desc = 3,
    iconAsset = 4,
}

local function asset_asset_get(id)
    local asset = require("cfgGen.asset.asset")
    return asset.get(id)
end


local name2ref = {
    iconAsset_ref = { 3, asset_asset_get },
}

local data = {
    ["dog Icon"] = { "dog Icon", "DOG", "this is a \"dog\" icon", 1, },
    ["catIcon"] = { "catIcon", "CAT", "i'm not sure,baby", 1, },
}

---@return asset.asset
function icon.get(id)
    return data[id]
end

util.setMetaGet(data, name2index, name2ref)

return icon